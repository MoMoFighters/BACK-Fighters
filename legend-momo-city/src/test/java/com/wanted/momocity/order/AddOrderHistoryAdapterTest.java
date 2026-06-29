package com.wanted.momocity.order;

import com.wanted.momocity.order.domain.model.Reason;
import com.wanted.momocity.order.domain.model.Type;
import com.wanted.momocity.order.infrastructure.adapter.AddOrderHistoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AddOrderHistoryAdapterTest {

    @Autowired
    private AddOrderHistoryAdapter adapter;

    @Test
    void 포인트_내역_저장() {
        adapter.saveOrderHistory(1L, Reason.COMPLETE, Type.GAINED, 1000L);
    }
}