package com.wanted.cleanarchitecture.catalog.presentation.api.request;

public record CreateCourseRequest(
      Long authorId,
      String title,
      String description
) {
}
