package com.spoonful.spoonful.recipe.repo;

import com.spoonful.spoonful.recipe.model.Recipe;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    @Query("""
    select r from Recipe r
    where (:q is null or lower(r.title) like lower(concat('%', :q, '%')))
    order by r.createdAt desc
  """)
    Page<Recipe> search(@Param("q") String q, Pageable pageable);
}