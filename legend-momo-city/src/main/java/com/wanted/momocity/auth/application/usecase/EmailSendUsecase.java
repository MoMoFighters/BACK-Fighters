package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.EmailSendCommand;
import com.wanted.momocity.auth.application.result.EmailSendResult;

public interface EmailSendUsecase {
    EmailSendResult emailSend(EmailSendCommand command);
}
