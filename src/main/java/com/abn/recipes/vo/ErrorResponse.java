package com.abn.recipes.vo;

import lombok.Builder;

@Builder(toBuilder = true)
public record ErrorResponse(String message) {
    public ErrorResponse(Exception e) {
        this(e.getMessage());
    }
}
