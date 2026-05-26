package com.wanted.momocity.calendar.application.usecase;

import com.wanted.momocity.calendar.application.command.DeleteMemoCommand;

/*
* comment.
*  Memo 삭제
* */

public interface DeleteMemoUseCase {

    void deleteMemo(DeleteMemoCommand command);

}
