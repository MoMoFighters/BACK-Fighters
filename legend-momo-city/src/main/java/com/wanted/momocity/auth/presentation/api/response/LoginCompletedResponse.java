package com.wanted.momocity.auth.presentation.api.response;

import com.wanted.momocity.auth.domain.model.Role;

public record LoginCompletedResponse(
        Role role,
        boolean is_tempPwd,
        String nickname

) {
}
