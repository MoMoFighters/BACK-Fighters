package com.wanted.momocity.user.application.command;

import com.wanted.momocity.global.domain.model.Category;
import org.springframework.web.multipart.MultipartFile;

public record TeacherApplyCommand(

        // 강사 신청 할 때
        Long userId,
        String currentNickname,
        String nickname,
        Category category,
        MultipartFile proof // 증빙자료 url

        // 여기서 MultipartFile 쓴 이유
        /* 기존에는 컨트롤러에서 S3에 강사 증빙자료를 업로드 하고 그 키값을 서비스로 넘김
        *  그러나 증빙자료 관련 정책과 비즈니스 로직들이 추가되며 이 작업은 서비스 계층으로 넘기게 됨
        *
        * 그러면 컨트롤러에서 기존에 String으로 키값을 넘겨주던 방식은 사용 불가능함
        * 그래서 command가 키값이 아닌 파일 자체를 받게 만듦
        *
        * 완전한 핵사고날 아키턱처 원칙에는 어긋나지만
        * 비즈니스로직은 서비스 계층에서 다루기 위해 트레이드 오프를 감수했다는 말임 ;*/

) {
}
