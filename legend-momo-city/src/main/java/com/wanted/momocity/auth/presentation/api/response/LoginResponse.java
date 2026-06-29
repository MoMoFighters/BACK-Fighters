package com.wanted.momocity.auth.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanted.momocity.auth.domain.model.Role;
import com.wanted.momocity.auth.domain.model.Status;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Status status,
        Role role,
        @JsonProperty("isTempPwd") boolean isTempPwd,
        /*comment
        *  record + boolean + is 접두사 조합에서 tempPwd로 나갈 수 있어서
        *  @JsonProperty 붙여서 나감  */
        String nickname,
        long expiresIn
) {



}
