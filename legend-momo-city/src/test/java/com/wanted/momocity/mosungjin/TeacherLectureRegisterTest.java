package com.wanted.momocity.mosungjin;

import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.domain.model.Lecture;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.presentation.api.TeacherLectureController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * TeacherLectureRegisterTest는 강사용 강의 등록 API를 검증하는 Controller 테스트다.
 *
 * 이 테스트는 실제 DB 저장이 아니라,
 * Controller가 요청을 받고 API 명세에 맞는 응답을 반환하는지 확인한다.
 */
@WebMvcTest(TeacherLectureController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeacherLectureRegisterTest {

    /*
     * MockMvc는 실제 서버를 띄우지 않고 HTTP 요청처럼 Controller를 테스트하게 해준다.
     */
    @Autowired
    private MockMvc mockMvc;

    /*
     * Controller가 의존하는 UseCase를 Mock으로 대체한다.
     *
     * Controller 테스트에서는 Service와 Repository를 실제로 실행하지 않는다.
     */
    @MockitoBean
    private LectureCommandUseCase lectureCommandUseCase;

    @Test
    @DisplayName("강사가 강의를 등록하면 201 응답을 반환한다")
    void createLecture() throws Exception {
        /*
         * 응답에 들어갈 생성일/수정일을 고정한다.
         *
         * 테스트에서는 시간이 매번 바뀌면 검증하기 어려우므로 고정값을 사용
         */
        LocalDateTime now = LocalDateTime.of(2026, 5, 19, 10, 30);

        /*
         * UseCase가 반환할 가짜 Lecture 객체를 준비
         *
         * 실제 저장 로직은 실행하지 않고,
         * 강의 등록이 성공했다고 가정
         */
        Lecture lecture = Lecture.restore(
                10L,
                3L,
                "Spring Boot 입문",
                "Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다.",
                "https://example.com/images/spring-boot.png",
                LectureCategory.STUDY,
                LectureStatus.WAITING,
                0,
                now,
                now
        );

        /*
         * lectureCommandUseCase.createLecture(...)가 호출되면
         * 위에서 만든 lecture를 반환하도록 설정
         */
        when(lectureCommandUseCase.createLecture(any()))
                .thenReturn(lecture);

        /*
         * API 명세에 맞춘 강의 등록 요청 JSON
         */
        String requestBody = """
                {
                  "title": "Spring Boot 입문",
                  "description": "Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다.",
                  "thumbnailUrl": "https://example.com/images/spring-boot.png",
                  "category": "STUDY"
                }
                """;

        /*
         * POST /api/teacher/lectures 요청을 보내고,
         * API 명세에 맞는 응답이 내려오는지 검증한다.
         */
        mockMvc.perform(post("/api/teacher/lectures")
                        .header("TeacherId", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("COMMON-CREATED"))
                .andExpect(jsonPath("$.message").value("강의가 등록되었습니다."))
                .andExpect(jsonPath("$.data.lectureId").value(10))
                .andExpect(jsonPath("$.data.teacherId").value(3))
                .andExpect(jsonPath("$.data.title").value("Spring Boot 입문"))
                .andExpect(jsonPath("$.data.description").value("Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다."))
                .andExpect(jsonPath("$.data.thumbnailUrl").value("https://example.com/images/spring-boot.png"))
                .andExpect(jsonPath("$.data.category").value("STUDY"))
                .andExpect(jsonPath("$.data.lectureStatus").value("WAITING"))
                .andExpect(jsonPath("$.data.completedUserCount").value(0))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    @DisplayName("강의 등록 시 필수값이 누락되면 400 응답을 반환한다")
    void createLectureFailByInvalidRequest() throws Exception {
        /*
         * title은 빈 문자열이고 category는 null
         * CreateLectureRequest의 @NotBlank, @NotNull 검증에 걸려
         * Controller 진입 전 400 Bad Request가 발생
         */
        String requestBody = """
            {
              "title": "",
              "description": "Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다.",
              "thumbnailUrl": "https://example.com/images/spring-boot.png",
              "category": null
            }
            """;

        /*
         * 잘못된 요청을 보내고 400 응답이 내려오는지 확인
         * 현재 ApiExceptionHandler는 첫 번째 validation error만 message에 담기 때문에,
         * title 또는 category 중 하나의 메시지만 내려올 수 있다.
         */
        mockMvc.perform(post("/api/teacher/lectures")
                        .header("TeacherId", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-VALIDATION-ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }
    @Test
    @DisplayName("강의 등록 시 허용되지 않은 카테고리면 400 응답을 반환한다")
    void createLectureFailByInvalidCategory() throws Exception {
        /*
         * CreateLectureRequest.toCommand()에서 LectureCategory 변환에 실패하면 400 에러 처리
         */
        String requestBody = """
            {
              "title": "Spring Boot 입문",
              "description": "Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다.",
              "thumbnailUrl": "https://example.com/images/spring-boot.png",
              "category": "INVALID"
            }
            """;

        /*
         * 잘못된 카테고리 요청을 보내고,
         * 명세에 맞게 400 응답과 카테고리 오류 메시지가 내려오는지 확인
         */
        mockMvc.perform(post("/api/teacher/lectures")
                        .header("TeacherId", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-DOMAIN-RULE-VIOLATION"))
                .andExpect(jsonPath("$.message").value("허용되지 않은 강의 카테고리입니다."));
    }
}