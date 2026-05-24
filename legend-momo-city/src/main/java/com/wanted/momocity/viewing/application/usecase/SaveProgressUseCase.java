package com.wanted.momocity.viewing.application.usecase;

import com.wanted.momocity.viewing.application.command.SaveProgressCommand;
import com.wanted.momocity.viewing.presentation.api.response.SaveProgressResponse;

/*
* comment.
*  시청 중 진척도 저장(5-10초 주기 호출)
* */

public interface SaveProgressUseCase {

    // 파라미터가 3개 이상, 묶어서 전달
    SaveProgressResponse saveProgress (SaveProgressCommand command);

}
