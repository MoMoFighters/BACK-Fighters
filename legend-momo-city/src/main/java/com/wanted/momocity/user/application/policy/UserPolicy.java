package com.wanted.momocity.user.application.policy;

import com.wanted.momocity.auth.application.port.PasswordEncodePort;
import com.wanted.momocity.user.domain.exception.*;
import com.wanted.momocity.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class UserPolicy {
    private final UserRepository userRepository;
    private final PasswordEncodePort passwordEncodePort;

    public void nicknamePolicy(String nickname) {

        if (userRepository.existsByNickname(nickname)) {
            throw new NicknameDuplicateException("이미 사용 중인 닉네임입니다.");
        }
    }

    // storedPassword :db 에 암호화해서 저장해둔 비밀버노
    public void passwordPolicy(String currentPassword, String newPassword, String storedPassword) {
        if (newPassword == null) return;

        // 비밀번호 변경할 때 기존 비밀번호 입력 안 하면 예외
        if (currentPassword == null) {
            throw new InvalidPasswordException("현재 비밀번호를 입력해주세요.");
        }

        // 기존 비밀번호 일치 확인
        if (!passwordEncodePort.matches(currentPassword, storedPassword)) {
            throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호가 기존과 같으면 예외
        if (passwordEncodePort.matches(newPassword, storedPassword)) {
            throw new SamePasswordException("기존 비밀번호와 일치합니다.");
        }
    }

    // 강사증빙자료 관련 정책
    public void teacherProofPolicy(MultipartFile proof) {
        // 증빙자료 제출 여부 확인
        if (proof == null || proof.isEmpty()) {
            throw new MissingProofException("증빙 자료는 필수 제출입니다.");
        }

        // 확장자 제한 - pdf/mp4만 가능
        String extension = proof.getOriginalFilename()
                .substring(proof.getOriginalFilename().lastIndexOf(".") + 1)
                .toLowerCase();

        if (!extension.equals("pdf") && !extension.equals("mp4")) {
            throw new InvalidFileExtensionException("pdf 또는 mp4 파일만 업로드 가능합니다.");
        }

    }
}
