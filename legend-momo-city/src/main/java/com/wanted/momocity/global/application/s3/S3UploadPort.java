package com.wanted.momocity.global.application.s3;

import org.springframework.web.multipart.MultipartFile;

public interface S3UploadPort {
    String upload(MultipartFile file);

}
