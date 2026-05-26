package com.wanted.momocity.calendar.application.usecase;

import com.wanted.momocity.calendar.application.command.UpdateTodoCommand;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;

/*
 * comment.
 *  Todo 수정
 * */

public interface UpdateTodoUseCase {

    TodoResponse updateTodo(UpdateTodoCommand command);

}
