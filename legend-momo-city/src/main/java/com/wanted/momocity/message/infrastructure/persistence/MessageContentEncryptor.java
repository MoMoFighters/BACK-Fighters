package com.wanted.momocity.message.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

//채팅 내역 암호화하는 클래스
@Slf4j
@Converter //autoApply = false(기본값) -> 명시적으로 @Convert 붙인 필드에만 적용
public class MessageContentEncryptor implements AttributeConverter<String, String>{

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    // [코드래빗 반영] 암호화된 값임을 명시하는 접두사 (평문/암호문 판별용)
    private static final String ENCRYPTED_PREFIX = "ENC::";

    private static final String SECRET_KEY = System.getenv("MESSAGE_ENCRYPTION_KEY");

    // [코드래빗 반영] SecureRandom은 스레드 안전 -> 클래스 로드 시 한 번만 생성해서 재사용
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // [코드래빗 반영] 매 호출마다 Base64 디코딩하지 않도록, 디코딩된 키를 한 번만 캐싱
    private static final SecretKeySpec KEY_SPEC;

    // [코드래빗 반영] 클래스 로드 시점(서버 기동 초기)에 키 존재 여부 및 길이를 검증해 fail-fast
    static {
        if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
            throw new IllegalStateException(
                    "MESSAGE_ENCRYPTION_KEY 환경변수가 설정되지 않았습니다. 서버를 시작할 수 없습니다."
            );
        }

        byte[] decodedKey;
        try {
            decodedKey = Base64.getDecoder().decode(SECRET_KEY);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "MESSAGE_ENCRYPTION_KEY가 올바른 Base64 형식이 아닙니다.", e
            );
        }

        // AES 키는 16(128비트), 24(192비트), 32(256비트) 바이트만 허용
        if (decodedKey.length != 16 && decodedKey.length != 24 && decodedKey.length != 32) {
            throw new IllegalStateException(
                    "MESSAGE_ENCRYPTION_KEY의 길이가 올바르지 않습니다. (현재 " + decodedKey.length + "바이트, 16/24/32바이트만 허용)"
            );
        }

        KEY_SPEC = new SecretKeySpec(decodedKey, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, KEY_SPEC, gcmSpec);

            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            //IV + 암호문을 합쳐서 하나의 문자열로 저장 (복호화 시 IV 필요하므로)
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            // 접두사를 붙여서 "이건 암호화된 값"임을 명확히 표시
            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("[MessageContentEncryptor] 메시지 암호화 실패", e);
            throw new IllegalStateException("메시지 암호화 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        // 접두사가 없으면 -> 추측 없이 확정적으로 평문(암호화 이전 데이터)으로 판단
        if (!dbData.startsWith(ENCRYPTED_PREFIX)) {
            return dbData;
        }

        try {
            // 접두사를 떼어낸 나머지 부분만 Base64 디코딩
            String base64Part = dbData.substring(ENCRYPTED_PREFIX.length());
            byte[] combined = Base64.getDecoder().decode(base64Part);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, KEY_SPEC, gcmSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            log.error("[MessageContentEncryptor] 메시지 복호화 실패", e);
            throw new IllegalStateException("메시지 복호화 중 오류가 발생했습니다.", e);
        }
    }
}
