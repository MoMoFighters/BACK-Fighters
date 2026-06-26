package com.wanted.momocity.global.application.s3;

public interface S3PresignedUrlPort {
    String generatePresignedUrl (String url);
}
