package com.wanted.momocity.message.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

//채팅 내역 암호화하는 클래스
@Slf4j
@Converter //autoApply = false(기본값) -> 명시적으로 @Convert 붙인 필드에만 적용
public class MessageContentEncryptor implements AttributeConverter<String, String>{

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    // 🚨 실제 운영에서는 환경변수/Vault/KMS 등에서 주입받아야 함. 하드코딩 금지.
    private static final String SECRET_KEY = System.getenv("MESSAGE_ENCRYPTION_KEY");

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(SECRET_KEY), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] encrypted = cipher.doFinal(attribute.getBytes());

            //IV + 암호문을 합쳐서 하나의 문자열로 저장 (복호화 시 IV 필요하므로)
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("[MessageContentEncryptor] 메시지 암호화 실패", e);
            throw new IllegalStateException("메시지 암호화 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(SECRET_KEY), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (IllegalArgumentException e) {
            // Base64 형식이 아니면 = 암호화 적용 전의 평문 데이터로 간주하고 그대로 반환
            log.warn("[MessageContentEncryptor] Base64 형식이 아닌 평문 데이터로 판단 - 그대로 반환");
            return dbData;
        } catch (Exception e) {
            log.error("[MessageContentEncryptor] 메시지 복호화 실패", e);
            throw new IllegalStateException("메시지 복호화 중 오류가 발생했습니다.", e);
        }
    }
}
