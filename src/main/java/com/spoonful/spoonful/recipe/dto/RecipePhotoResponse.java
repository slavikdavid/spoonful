package com.spoonful.spoonful.recipe.dto;

public record RecipePhotoResponse(
        Long id,
        String url,
        boolean cover,
        int sortOrder
) {}