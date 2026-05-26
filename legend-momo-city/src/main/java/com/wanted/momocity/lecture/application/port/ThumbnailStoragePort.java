package com.wanted.momocity.lecture.application.port;

import com.wanted.momocity.lecture.application.command.LectureThumbnailFile;
import org.springframework.stereotype.Component;

// ThumbnailStoragePort는 강의 썸네일 파일 저장소
@Component // 안하면 서비스에 주입 안돼...
public interface ThumbnailStoragePort {

    String upload(LectureThumbnailFile thumbnailFile);
}