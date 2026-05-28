package com.wanted.momocity.viewing.application.usecase;

import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.presentation.api.response.SaveProgressResponse;

/*
 * comment.
 *  Viewing 컨텍스트의 쓰기 작업 전용 UseCase
 *  → 상태를 변경하는 작업만 담당
 *  handle() 로 Command 처리
 *  → Controller 에서 ViewingCommandUseCase 1개만 주입
 *  -
 *  [담당 Command]
 *  - SaveProgressCommand : 진척도 저장
 */

public interface ViewingCommandUseCase {

    SaveProgressResponse handle(SaveProgressCommand command);

}
