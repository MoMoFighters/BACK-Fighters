package com.wanted.momocity.auth.application.command;

public record LoginCommand(
        String email,
        String password
) {
}
