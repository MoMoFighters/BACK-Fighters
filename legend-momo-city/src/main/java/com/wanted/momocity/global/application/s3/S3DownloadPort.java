package com.wanted.momocity.global.application.s3;

public interface S3DownloadPort {
    byte[] download(String s3Key);

}
