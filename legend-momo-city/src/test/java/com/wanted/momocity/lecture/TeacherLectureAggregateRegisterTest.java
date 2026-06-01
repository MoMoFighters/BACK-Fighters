package com.wanted.momocity.lecture;

import com.wanted.momocity.global.application.s3.S3UploadPort;
import com.wanted.momocity.lecture.application.usecase.AdminLectureCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.AdminLectureQueryUseCase;
import com.wanted.momocity.lecture.application.usecase.ChapterCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.momocity.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.momocity.lecture.domain.model.LectureAggregate;
import com.wanted.momocity.lecture.domain.model.LectureChapter;
import com.wanted.momocity.lecture.domain.model.LectureCategory;
import com.wanted.momocity.lecture.domain.model.LectureStatus;
import com.wanted.momocity.lecture.domain.model.VideoStatus;
import com.wanted.momocity.lecture.presentation.api.LectureController;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * 강사 강의 등록 API Controller 테스트.
 * 실제 S3, DB, JWT 검증은 사용하지 않고 Controller 요청/응답 흐름만 검증한다.
 */
@WebMvcTest(LectureController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeacherLectureAggregateRegisterTest {

    private static final String CREATE_LECTURE_URL = "/api/v1/lectures";

    @Autowired
    private MockMvc mockMvc;

    // 강의 등록 UseCase를 Mock으로 대체한다.
    @MockitoBean
    private LectureCommandUseCase lectureCommandUseCase;

    // TeacherLectureController 생성자 의존성 때문에 필요하다.
    @MockitoBean
    private ChapterCommandUseCase chapterCommandUseCase;

    // TeacherLectureController 생성자 의존성 때문에 필요하다.
    @MockitoBean
    private LectureQueryUseCase lectureQueryUseCase;

    @MockitoBean
    private AdminLectureQueryUseCase adminLectureQueryUseCase;

    @MockitoBean
    private AdminLectureCommandUseCase adminLectureCommandUseCase;

    // 실제 S3 업로드 대신 URL만 반환하도록 Mock 처리한다.
    @MockitoBean
    private S3UploadPort s3UploadPort;

    @Test
    @DisplayName("강사가 form-data로 강의를 등록하면 201 Created를 반환한다")
    void createLectureSuccess() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 5, 19, 10, 30);
        String thumbnailUrl = "https://example.com/images/spring-boot.png";

        // S3 업로드 성공 시 썸네일 URL이 반환된다고 가정한다.
        when(s3UploadPort.upload(any()))
                .thenReturn(thumbnailUrl);

        // 강의 등록 UseCase가 반환할 도메인 객체를 준비한다.
        LectureAggregate lecture = LectureAggregate.restore(
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

        // multipart/form-data의 thumbnail 파일 파트.
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
                .andExpect(jsonPath("$.message").exists())
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

        verify(s3UploadPort).upload(any());
        verify(lectureCommandUseCase).createLecture(any());
    }

    @Test
    @DisplayName("강사가 챕터 동영상을 등록하면 업로드 UseCase로 파일을 전달한다")
    void registerChapterVideoSuccess() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 5, 19, 11, 0);
        String videoUrl = "https://momocity-bucket.s3.ap-northeast-2.amazonaws.com/video.mp4";

        LectureChapter chapter = LectureChapter.restore(
                20L,
                10L,
                "1강. OT",
                1,
                videoUrl,
                10L,
                120,
                VideoStatus.UPLOADING,
                "lesson.mp4",
                now,
                now
        );

        when(chapterCommandUseCase.registerChapterVideo(any()))
                .thenReturn(chapter);

        MockMultipartFile video = new MockMultipartFile(
                "video",
                "lesson.mp4",
                "video/mp4",
                "fake-video".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/lectures/{lectureId}/chapters/{chapterId}/video", 10L, 20L)
                        .file(video)
                        .param("durationSec", "120")
                        .principal(teacherAuthentication())
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("COMMON-SUCCESS"))
                .andExpect(jsonPath("$.data.chapterId").value(20))
                .andExpect(jsonPath("$.data.lectureId").value(10))
                .andExpect(jsonPath("$.data.videoUrl").value(videoUrl))
                .andExpect(jsonPath("$.data.videoSizeBytes").value(10))
                .andExpect(jsonPath("$.data.durationSec").value(120))
                .andExpect(jsonPath("$.data.videoStatus").value("UPLOADING"))
                .andExpect(jsonPath("$.data.originalFilename").value("lesson.mp4"));

        verify(chapterCommandUseCase).registerChapterVideo(any());
    }

    @Test
    @DisplayName("강의 등록 시 제목이 비어 있으면 400 Bad Request를 반환한다")
    void createLectureFailByBlankTitle() throws Exception {
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
                .andExpect(status().isBadRequest());

        // 입력값 검증에서 막혀야 하므로 S3 업로드와 UseCase 호출은 발생하면 안 된다.
        verify(s3UploadPort, never()).upload(any());
        verify(lectureCommandUseCase, never()).createLecture(any());
    }

    @Test
    @DisplayName("강의 등록 시 허용되지 않은 카테고리면 400 Bad Request를 반환하고 S3 업로드를 하지 않는다")
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
                .andExpect(jsonPath("$.message").exists());

        // category 검증은 S3 업로드보다 먼저 수행되어야 한다.
        verify(s3UploadPort, never()).upload(any());
        verify(lectureCommandUseCase, never()).createLecture(any());
    }

    @Test
    @DisplayName("강의 등록 시 썸네일 파일이 없으면 400 Bad Request를 반환한다")
    void createLectureFailWithoutThumbnail() throws Exception {
        mockMvc.perform(multipart(CREATE_LECTURE_URL)
                        .param("title", "Spring Boot 입문")
                        .param("description", "Spring Boot 기초부터 REST API 개발까지 배우는 강의입니다.")
                        .param("category", "STUDY")
                        .principal(teacherAuthentication()))
                .andExpect(status().isBadRequest());

        // 썸네일이 없으면 S3 업로드와 UseCase 호출이 발생하면 안 된다.
        verify(s3UploadPort, never()).upload(any());
        verify(lectureCommandUseCase, never()).createLecture(any());
    }

    /*
     * 현재 Controller는 authentication.getName()을 Long으로 변환한다.
     * 따라서 테스트 principal도 email이 아니라 userId 문자열로 둔다.
     */
    private UsernamePasswordAuthenticationToken teacherAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                "3",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
    }
}
