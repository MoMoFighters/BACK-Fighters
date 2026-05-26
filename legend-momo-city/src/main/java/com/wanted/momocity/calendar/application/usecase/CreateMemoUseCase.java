package com.wanted.momocity.calendar.application.usecase;

import com.wanted.momocity.calendar.application.command.CreateMemoCommand;
import com.wanted.momocity.calendar.presentation.api.response.MemoResponse;

/*
 * comment.
 *  Memo 등록
 * */

public interface CreateMemoUseCase {

    MemoResponse createMemo(CreateMemoCommand command);

}
