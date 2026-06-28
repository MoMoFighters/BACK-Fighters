package com.wanted.momocity.store.application.port;

import java.util.List;

public interface GetUserOwnedItemIdPort {
    List<Long> userOwnedItemId(Long userId);
}
