package com.wanted.momocity.auth.application.usecase;

import com.wanted.momocity.auth.application.command.EmailSendCommand;

public interface TempPasswordUsecase {

    void sendTempPassword(EmailSendCommand command);

}
