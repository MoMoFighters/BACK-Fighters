package com.wanted.momocity.message;

import com.wanted.momocity.friend.fmexception.FMResourceNotFoundException;
import com.wanted.momocity.friend.user.UserWithFMJpaEntity;
import com.wanted.momocity.message.application.manager.ChatRoomSessionManager;
import com.wanted.momocity.message.application.policy.MessageEligibilityPolicy;
import com.wanted.momocity.message.application.service.SendMessageCommandService;
import com.wanted.momocity.message.application.usecase.SendMessageCommandUseCase; 
import com.wanted.momocity.message.domain.event.SendMessagePublishedEvent;
import com.wanted.momocity.message.domain.repository.MessageRepository;
import com.wanted.momocity.message.infrastructure.persistence.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction; 
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

//Mockito 프레임워크를 확장 적용(가짜 객체 활성화)
@ExtendWith(MockitoExtension.class)
@DisplayName("SendMessageCommandService 단위 테스트")
public class SendMessageCommandServiceTest {

    //실제 테스트 대상이 되는 클래스
    //가짜 객체들이 이 서비스 내부의 의존성으로 주입됨.
    @InjectMocks
    private SendMessageCommandService sendMessageCommandService;

    //가짜 객체
    @Mock private MessageRepository messageRepository;
    @Mock private MessageSideFriendRepository messageSideFriendRepository;
    @Mock private MessageEligibilityPolicy messageEligibilityPolicy;
    @Mock private MessageSideUserRepository messageSideUserRepository;
    @Mock private ApplicationEventPublisher eventPublisher; //알림 이벤트 가짜 객체
    @Mock private ChatRoomSessionManager sessionManager; //웹소켓 세션 가짜 객체
    @Mock private SimpMessagingTemplate messagingTemplate; //실시간 소켓 전송 가짜 객체

    //@Nested: 기능별로 테스트 케이스를 묶어줌
    @Nested
    @DisplayName("메시지 전송 시")
    class HandleMethod {

        //테스트에서 공통으로 사용할 가짜 요청 데이터
        Long senderId = 1L;
        Long targetUserId = 2L;
        Long roomId = 100L;
        String content = "안녕하세요!";

        @Test
        @DisplayName("성공: 상대방이 방에 없을 때 안읽음(false) 상태로 메시지가 저장되고 웹소켓/이벤트가 정상 발행된다")
        void success_TargetUserAbsent() {
            //엔티티도 가짜로 생성
            // given
            UserWithFMJpaEntity sender = mock(UserWithFMJpaEntity.class);
            ChatRoomJpaEntity chatRoom = mock(ChatRoomJpaEntity.class);
            ChatRoomMemberJpaEntity member1 = mock(ChatRoomMemberJpaEntity.class);
            ChatRoomMemberJpaEntity member2 = mock(ChatRoomMemberJpaEntity.class);
            UserWithFMJpaEntity targetUser = mock(UserWithFMJpaEntity.class);

            //가짜 객체들이 getId 등을 호출할 때 돌려줄 값
            given(sender.getId()).willReturn(senderId);
            given(sender.getNickname()).willReturn("보낸이");
            given(targetUser.getId()).willReturn(targetUserId);

            //채팅방에 나와 상대방이 들어있는 상황 가정
            given(member1.getUserId()).willReturn(sender);
            given(member2.getUserId()).willReturn(targetUser);
            List<ChatRoomMemberJpaEntity> members = List.of(member1, member2);

            //서비스 내부와 조회 메서드가 만났을 때 뱉을 응답
            given(messageSideUserRepository.findUserById(senderId)).willReturn(Optional.of(sender));
            given(messageRepository.findChatRoomById(roomId)).willReturn(Optional.of(chatRoom));
            given(messageRepository.findMembersByRoomId(roomId)).willReturn(members);

            //양방향 친구 관계는 존재하지 않는 상태로 가정(행 무존재)
            given(messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(senderId, targetUserId)).willReturn(Optional.empty());
            given(messageSideFriendRepository.findByFromUserId_IdAndToUserId_Id(targetUserId, senderId)).willReturn(Optional.empty());

            //상대방이 웹소켓 방에 현재 없다면 안읽음 처리가 되는지 테스트
            given(sessionManager.isUserInRoom(targetUserId, roomId)).willReturn(false);

            //mockConstruction: 서비스 내부에서 새로 생성되는 것까지 가짜로 만듦
            try (MockedConstruction<MessageJpaEntity> mocked = mockConstruction(MessageJpaEntity.class, (mock, context) -> {
                given(mock.getId()).willReturn(500L);
            })) {

                //가짜로 세팅된 환경으로 진짜 서비스 구동
                // when
                SendMessageCommandUseCase.SendView result = sendMessageCommandService.handle(senderId, roomId, content);

                //상태 검증: 서비스가 최종 반환한 값에 방 번호와 내용을 담고 있는지 검사
                // then
                assertThat(result).isNotNull();
                assertThat(result.roomId()).isEqualTo(roomId);
                assertThat(result.content()).isEqualTo(content);

                //행위 검증: 리턴값만 확인하는 게 아니라 진짜로 실행됐는지 감시
                //정책 검증 클래스가 한 번 호출됐는지 검사
                verify(messageEligibilityPolicy, times(1)).sendable(eq(roomId), eq(senderId), any(), eq(2L));

                //프론트엔드가 구독하는 웹소켓 채널로 한 번 신호가 쏴졌는지 검사
                verify(messagingTemplate, times(1)).convertAndSend(eq("/sub/chat/room/" + roomId), any(SendMessageCommandService.WebSocketMessageDto.class));

                //notification에 알림을 추가하라고 이벤트 한 번 발행했는지 검사
                verify(eventPublisher, times(1)).publishEvent(any(SendMessagePublishedEvent.class));
            }
        }

        @Test
        @DisplayName("예외: 발신자가 시스템에 존재하지 않으면 404 커스텀 예외를 던진다")
        void fail_SenderNotFound() {
            //보낸 사람이나 내가 DB조회 결과에 없는 상황 가정
            // given
            given(messageSideUserRepository.findUserById(senderId)).willReturn(Optional.empty());

            //커스텀 404 예외가 터지는지 검증
            // when & then
            assertThatThrownBy(() -> sendMessageCommandService.handle(senderId, roomId, content))
                    .isInstanceOf(FMResourceNotFoundException.class)
                    .hasMessageContaining("존재하지 않는 사용자입니다."); //에러 메시지 문구까지 정확한지 검증

            //보낸 사람이 없으면 밑에 있는 채팅방 조회까지 도달하지 않고 폭파되었는지 확인
            verify(messageRepository, never()).findChatRoomById(any());
        }

        @Test
        @DisplayName("예외: 채팅방이 존재하지 않으면 404 커스텀 예외를 던진다")
        void fail_ChatRoomNotFound() {
            // given
            UserWithFMJpaEntity sender = mock(UserWithFMJpaEntity.class);
            //보낸 사람은 로그인되었지만 채팅방 아이디가 DB에 없는 상황 가정
            given(messageSideUserRepository.findUserById(senderId)).willReturn(Optional.of(sender));
            given(messageRepository.findChatRoomById(roomId)).willReturn(Optional.empty());

            //방이 없으므로 커스텀 404 에러가 터지는지 확인
            // when & then
            assertThatThrownBy(() -> sendMessageCommandService.handle(senderId, roomId, content))
                    .isInstanceOf(FMResourceNotFoundException.class)
                    .hasMessageContaining("존재하지 않는 채팅방입니다.");

            //방어 확인: 방이 없으므로 멤버를 모으는 작업은 실행하지 않음
            verify(messageRepository, never()).findMembersByRoomId(any());
        }
    }
}
