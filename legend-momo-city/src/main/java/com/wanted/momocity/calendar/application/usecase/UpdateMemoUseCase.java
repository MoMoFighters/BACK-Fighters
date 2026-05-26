package com.wanted.momocity.calendar.application.usecase;

import com.wanted.momocity.calendar.application.command.UpdateMemoCommand;
import com.wanted.momocity.calendar.presentation.api.response.MemoResponse;

/*
 * comment.
 *  Memo 수정
 * */

public interface UpdateMemoUseCase {

    MemoResponse updateMemo(UpdateMemoCommand command);

}
