package com.wanted.momocity.user.infrastructure.drive;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.wanted.momocity.user.application.port.GoogleDriveUploadPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

@Slf4j
@Component
public class GoogleDriveUploadAdapter implements GoogleDriveUploadPort {

    @Value("${google.drive.client-id}")
    private String clientId;

    @Value("${google.drive.client-secret}")
    private String clientSecret;

    @Value("${google.drive.refresh-token}")
    private String refreshToken;

    @Value("${google.drive.folder-id}")
    private String folderId;

    @Override
    public void uploadGoogleDrive(MultipartFile file, String fileName) {
        try {
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setClientSecrets(clientId, clientSecret)
                    .build()
                    .setRefreshToken(refreshToken);

            Drive drive = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("momocity")
                    .build();

            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(folderId));

            InputStreamContent mediaContent = new InputStreamContent(
                    file.getContentType(),
                    file.getInputStream()
            );

            drive.files().create(fileMetadata, mediaContent)
                    .setFields("id, name")
                    .execute();

            log.info("[drive] 파일 업로드 완료 | fileName={}", fileName);

        } catch (Exception e) {
            log.error("[drive] 파일 업로드 실패 | fileName={} | error={}", fileName, e.getMessage());
            throw new RuntimeException("Google Drive 업로드 실패", e);
        }
    }
}