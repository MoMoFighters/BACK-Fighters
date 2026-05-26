package com.wanted.momocity.mosungjin;

import com.wanted.momocity.global.application.s3.S3UploadPort;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * TeacherLectureRegisterTest는 강사용 강의 등록 API를 검증하는 Controller 테스트다.
 *
 * 실제 JWT 검증, 실제 S3 업로드, 실제 DB 저장은 수행하지 않고,
 * multipart/form-data 요청이 Controller에서 정상 처리되는지 확인한다.
 */
@WebMvcTest(TeacherLectureController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeacherLectureRegisterTest {

    private static final String CREATE_LECTURE_URL = "/api/v1/teacher/lectures";

    /*
     * MockMvc는 실제 서버를 띄우지 않고 HTTP 요청처럼 Controller를 테스트하게 해준다.
     */
    @Autowired
    private MockMvc mockMvc;

    /*
     * Controller가 의존하는 UseCase를 Mock으로 대체한다.
     */
    @MockitoBean
    private LectureCommandUseCase lectureCommandUseCase;

    /*
     * 실제 S3 업로드 대신 Mock으로 URL을 반환하게 한다.
     */
    @MockitoBean
    private S3UploadPort s3UploadPort;

    @Test
    @DisplayName("강사가 form-data로 강의를 등록하면 201 응답을 반환한다")
    void createLecture() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 5, 19, 10, 30);
        String thumbnailUrl = "https://example.com/images/spring-boot.png";

        /*
         * S3 업로드가 성공하면 thumbnailUrl을 반환한다고 가정한다.
         */
        when(s3UploadPort.upload(any()))
                .thenReturn(thumbnailUrl);

        /*
         * UseCase가 반환할 가짜 Lecture 객체를 준비한다.
         */
        Lecture lecture = Lecture.restore(
                10L,
                3L,
                "Spring Boot 입문",
                "Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다.",
                thumbnailUrl,
                LectureCategory.STUDY,
                LectureStatus.WAITING,
                0,
                now,
                now
        );

        when(lectureCommandUseCase.createLecture(any()))
                .thenReturn(lecture);

        /*
         * form-data의 thumbnail 파일 파트다.
         */
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "spring-boot.png",
                "image/png",
                "fake-image".getBytes()
        );

        mockMvc.perform(multipart(CREATE_LECTURE_URL)
                        .file(thumbnail)
                        .param("title", "Spring Boot 입문")
                        .param("description", "Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다.")
                        .param("category", "STUDY")
                        .principal(teacherAuthentication()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("COMMON-CREATED"))
                .andExpect(jsonPath("$.message").value("강의가 등록되었습니다."))
                .andExpect(jsonPath("$.data.lectureId").value(10))
                .andExpect(jsonPath("$.data.teacherId").value(3))
                .andExpect(jsonPath("$.data.title").value("Spring Boot 입문"))
                .andExpect(jsonPath("$.data.description").value("Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다."))
                .andExpect(jsonPath("$.data.thumbnailUrl").value(thumbnailUrl))
                .andExpect(jsonPath("$.data.category").value("STUDY"))
                .andExpect(jsonPath("$.data.lectureStatus").value("WAITING"))
                .andExpect(jsonPath("$.data.completedUserCount").value(0))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    @DisplayName("강의 등록 시 필수값이 누락되면 400 응답을 반환한다")
    void createLectureFailByInvalidRequest() throws Exception {
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "spring-boot.png",
                "image/png",
                "fake-image".getBytes()
        );

        mockMvc.perform(multipart(CREATE_LECTURE_URL)
                        .file(thumbnail)
                        .param("title", "")
                        .param("description", "Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다.")
                        .param("category", "STUDY")
                        .principal(teacherAuthentication()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-VALIDATION-ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("강의 등록 시 허용되지 않은 카테고리면 400 응답을 반환하고 S3 업로드를 하지 않는다")
    void createLectureFailByInvalidCategory() throws Exception {
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "spring-boot.png",
                "image/png",
                "fake-image".getBytes()
        );

        mockMvc.perform(multipart(CREATE_LECTURE_URL)
                        .file(thumbnail)
                        .param("title", "Spring Boot 입문")
                        .param("description", "Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다.")
                        .param("category", "INVALID")
                        .principal(teacherAuthentication()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-DOMAIN-RULE-VIOLATION"))
                .andExpect(jsonPath("$.message").value("허용되지 않은 강의 카테고리입니다."));

        /*
         * category 검증이 S3 업로드보다 먼저 실행되어야 한다.
         */
        verify(s3UploadPort, never()).upload(any());
    }

    @Test
    @DisplayName("강의 등록 시 썸네일 파일이 없으면 400 응답을 반환한다")
    void createLectureFailWithoutThumbnail() throws Exception {
        mockMvc.perform(multipart(CREATE_LECTURE_URL)
                        .param("title", "Spring Boot 입문")
                        .param("description", "Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다.")
                        .param("category", "STUDY")
                        .principal(teacherAuthentication()))
                .andExpect(status().isBadRequest());
    }

    /*
     * 강사 권한을 가진 인증 객체를 만든다.
     *
     * principal 값인 teacher@example.com은 실제 JWT subject에 해당하는 email 역할이다.
     */
    private UsernamePasswordAuthenticationToken teacherAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                "teacher@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
    }
}