package com.wanted.momocity.auth.application.command;

public record EmailVerifyCommand(
        String email,
        String code
) {
}
