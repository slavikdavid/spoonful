package com.spoonful.spoonful.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import java.util.List;

public record RecipeUpdateRequest(
        @NotBlank String title,
        String description,
        @Min(0) Integer prepMinutes,
        @Min(0) Integer cookMinutes,
        List<@NotBlank String> steps,
        List<@NotBlank String> tags
) {}