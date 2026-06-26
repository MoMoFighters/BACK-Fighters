package com.wanted.momocity.admin.application.usecase;

import com.wanted.momocity.admin.application.command.CreateNoticeCommand;

import java.util.List;

/* comment.
    AdminNoticeCommandUseCase
    AdminNoticeController 가 공지 작성 또는 수정을 요청할 때 어디에 요청을 해야하는지 정의하는 계약
    extends 가 필요없는 이유는 해당 인터페이스는 공지 관련해서 쓰기 작업만 정의하면 되기 때문에
    상속 받을 것이 없다.
 */

public interface AdminNoticeCommandUseCase {

    // MS-11 공지 작성
    void createNotice(CreateNoticeCommand command);

    // MS-17 공지 수정
    void updateNotice(Long id, String title, String content);

    // MS-18 공지 단건 삭제
    void deleteNotice(Long id);

    // MS-19 공지 선택 삭제
    void deleteNotices(List<Long> ids);

}
