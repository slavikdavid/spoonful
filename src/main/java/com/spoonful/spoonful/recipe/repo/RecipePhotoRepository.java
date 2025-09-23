package com.spoonful.spoonful.recipe.repo;

import com.spoonful.spoonful.recipe.model.RecipePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecipePhotoRepository extends JpaRepository<RecipePhoto, Long> {
    List<RecipePhoto> findByRecipeIdOrderBySortOrderAscIdAsc(Long recipeId);
    long countByRecipeId(Long recipeId);
}