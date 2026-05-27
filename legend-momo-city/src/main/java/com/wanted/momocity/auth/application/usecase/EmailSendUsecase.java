package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.EmailSendCommand;
import com.wanted.momocity.auth.presentation.api.response.EmailSendResponse;

public interface EmailSendUsecase {
    EmailSendResponse emailSend(EmailSendCommand command);
}
