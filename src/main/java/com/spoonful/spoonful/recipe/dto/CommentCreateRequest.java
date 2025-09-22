package com.spoonful.spoonful.recipe.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank String body
) {}