package com.wanted.momocity.user.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface GoogleDriveUploadPort {
    void uploadGoogleDrive(MultipartFile file, String fileName);

}
