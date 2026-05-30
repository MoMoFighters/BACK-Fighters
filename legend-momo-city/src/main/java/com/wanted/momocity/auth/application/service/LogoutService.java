package com.wanted.momocity.auth.application.service;

import com.wanted.momocity.auth.application.command.LogoutCommand;
import com.wanted.momocity.auth.application.port.BlacklistPort;
import com.wanted.momocity.auth.application.port.RedisRefreshTokenPort;
import com.wanted.momocity.auth.application.port.TokenProviderPort;
import com.wanted.momocity.auth.application.usecase.LogoutUsecase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutService implements LogoutUsecase {

        private final RedisRefreshTokenPort redisRefreshTokenPort;
        private final BlacklistPort blacklistPort;
        private final TokenProviderPort tokenProviderPort;


        @Override
        public void logout(LogoutCommand command) {
            // RefreshToken 삭제
            redisRefreshTokenPort.deleteByToken(command.refreshToken());

            // AccessToken 블랙리스트 등록 (로그아웃 하고 남은 만료시간만큼만 블랙리스트로)
            long remainingMillis = tokenProviderPort.getRemainingMillis(command.accessToken());
            if (remainingMillis > 0) {
                blacklistPort.addBlacklist(command.accessToken(), remainingMillis);
            }

            log.info("[logout] 로그아웃 완료 | remainingMillis={}", remainingMillis);
        }

}
