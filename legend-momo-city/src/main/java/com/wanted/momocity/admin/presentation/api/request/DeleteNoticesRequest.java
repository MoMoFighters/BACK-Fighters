package com.wanted.momocity.admin.presentation.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DeleteNoticesRequest(
        @NotNull @Size(min = 1) List<Long> ids
        ) {
}
