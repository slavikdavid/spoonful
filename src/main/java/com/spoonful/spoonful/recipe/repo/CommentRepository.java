package com.spoonful.spoonful.recipe.repo;

import com.spoonful.spoonful.recipe.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByRecipeIdAndParentIsNullOrderByCreatedAtAsc(Long recipeId);
}