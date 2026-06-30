package com.wanted.momocity.user.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface GoogleDriveUploadPort {
    void uploadGoogleDrive(byte[] fileBytes, String contentType, String fileName);

}
