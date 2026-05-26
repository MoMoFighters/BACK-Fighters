package com.wanted.momocity.calendar.application.usecase;

import com.wanted.momocity.calendar.application.command.DeleteTodoCommand;

/*
 * comment.
 *  Todo 삭제
 * */

public interface DeleteTodoUseCase {

    void deleteTodo(DeleteTodoCommand command);

}
