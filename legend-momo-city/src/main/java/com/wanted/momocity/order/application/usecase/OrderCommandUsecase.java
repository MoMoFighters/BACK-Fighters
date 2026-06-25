package com.wanted.momocity.order.application.usecase;

import com.wanted.momocity.order.application.command.MakeOrderCommand;

public interface OrderCommandUsecase {

    // 상품 구매 = 프사변경
    void makeOrder(MakeOrderCommand makeOrderCommand);
}
