package com.wanted.momocity.message.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wanted.momocity.message.application.usecase.MessageQueryUseCase.MessageHistoryView;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetMessageHistoryResponse(
        RoomInfo roomInfo,
        List<MessageDetail> messages
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RoomInfo(
            Long roomId,
            String targetNickname,
            String targetRole,
            String targetName,
            String targetLectureTitle,
            String targetProfileImageUrl,
            boolean isLeftRoom
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MessageDetail(
            Long messageId,
            Long senderId,
            String name,
            String nickname,
            String lectureTitle,
            String role,
            String status,
            String content,
            LocalDateTime createdAt,
            boolean isRead,
            boolean isMine,
            boolean isLeftRoom,
            String profileImageUrl
    ) {}

    public static GetMessageHistoryResponse of(Long roomId, List<MessageHistoryView> views) {

        List<MessageDetail> detailList = views.stream()
                .filter(view -> view.messageId() != null) // 💡 유령 객체 생성 원천 차단!
                .map(view -> {
        // 🎯 레퍼런스(목록 DTO) 정책 미러링 가공 시작
        String displayNickname = view.nickname();
        String finalLectureTitle = null;

        // 상대방이 보낸 말풍선 가공 규칙 적용
        if ("me".equals(view.status())) {
//            displayNickname = "나와의 채팅" + "(" + displayNickname + ")";
            //v2 -> 채팅방 이름을 띄울 것이므로 나와의 채팅 가공 필요 없음
        } else if (view.isMine()) {
            // 내가 보낸 메시지인 경우 마스킹 정책에서 제외하고 내 닉네임 그대로 유지
            displayNickname = view.nickname();
        }
        if (!"me".equals(view.status()) && !view.isMine()) {
            //내가 쓴 글이나 나와의 채팅이 아닌, 상대방 메시지 가공
            if (!view.isNotActive() && (!"FRIEND".equals(view.status()) || displayNickname.isEmpty() || view.isLeftRoom())) {
                // ACTIVE가 아니거나 차단, 친구 삭제(none) 상태일 때 "(알 수 없음)" 결합
                // v2-> ACTIVE이면서 친구가 아니거나 채팅방 나간 경우 가공
                if (displayNickname.isEmpty()) {
                    //상대 식별 불가면 (알 수 없음)
                    displayNickname = "(알 수 없음)";
                } else {
                    //상대 식별 가능하면 닉네임(알 수 없음)
                    displayNickname += "(알 수 없음)";
                }
            }
        }


        // 강의명 가공 소스 이식
        List<String> lectureTitle = view.lectureTitle();
        if (lectureTitle != null && !lectureTitle.isEmpty()) {
            finalLectureTitle = "(" + String.join(", ", lectureTitle) + ")";
        }

        return new MessageDetail(
                view.messageId(),
                view.senderId(),
                "TEACHER".equals(view.role()) && !view.isMine() ? view.name() : null, // 강사일 때만 실제 성함 노출
                displayNickname,
                finalLectureTitle,
                view.role(),
                view.status(),
                view.content(),
                view.createdAt(), // T 문자열 그대로 노출
                view.isRead(),    // true
                view.isMine(),
                view.isLeftRoom(),
                view.profileImageUrl()
        );
    }).toList();

        // 2. 상단 roomInfo 단 한 번만 매핑할 상대방 메타데이터 가공 (프라이버시 분기 이식)
        String targetNickname = null;
        String targetRole = null;
        String targetName = null;
        String targetLectureTitle = null;
        String targetProfileImageUrl = null;
        boolean isLeftRoom = false;

        if (!views.isEmpty()) {
            MessageHistoryView ref = views.get(0); // 데이터 파이프라인에서 추출
            targetRole = ref.role();
            targetProfileImageUrl = ref.profileImageUrl();

            if ("TEACHER".equals(ref.role())) {
                targetName = ref.name();
                targetNickname = ref.nickname();
                if (ref.lectureTitle() != null && !ref.lectureTitle().isEmpty()) {
                    targetLectureTitle = "(" + String.join(", ", ref.lectureTitle()) + ")";
                }
            } else if ("STUDENT".equals(ref.role())) {
                targetName = null; // 학생 이름 프라이버시 원천 숨김
                targetNickname = ref.nickname();
                targetLectureTitle = null;
            }
        }

        RoomInfo roomInfo = new RoomInfo(roomId, targetNickname, targetRole, targetName, targetLectureTitle, targetProfileImageUrl, isLeftRoom);
        return new GetMessageHistoryResponse(roomInfo, detailList);
    }
}
