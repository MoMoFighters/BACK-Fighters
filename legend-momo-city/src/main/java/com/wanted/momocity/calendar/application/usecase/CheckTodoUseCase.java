package com.wanted.momocity.calendar.application.usecase;

import com.wanted.momocity.calendar.application.command.CheckTodoCommand;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;

/*
 * comment.
 *  Todo 체크 상태 변경
 * */

public interface CheckTodoUseCase {

    TodoResponse checkTodo(CheckTodoCommand command);

}
