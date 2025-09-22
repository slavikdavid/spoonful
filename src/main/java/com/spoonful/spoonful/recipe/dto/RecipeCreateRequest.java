package com.spoonful.spoonful.recipe.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record RecipeCreateRequest(
        @NotBlank String title,
        String description,
        @PositiveOrZero int prepMinutes,
        @PositiveOrZero int cookMinutes,
        @NotEmpty List<@NotBlank String> steps,
        List<@NotBlank String> tags
) {}