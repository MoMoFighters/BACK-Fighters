package com.wanted.momocity.calendar.application.usecase;

import com.wanted.momocity.calendar.application.command.CreateTodoCommand;
import com.wanted.momocity.calendar.presentation.api.response.TodoResponse;

/*
 * comment.
 *  Todo 등록
 * */

public interface CreateTodoUseCase {

    TodoResponse createTodo(CreateTodoCommand command);

}
