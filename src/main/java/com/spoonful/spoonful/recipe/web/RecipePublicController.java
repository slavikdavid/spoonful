package com.spoonful.spoonful.recipe.web;

import com.spoonful.spoonful.recipe.model.Recipe;
import com.spoonful.spoonful.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes/public")
@RequiredArgsConstructor
public class RecipePublicController {
    private final RecipeRepository recipes;

    @GetMapping("/search")
    public Page<Recipe> search(@RequestParam(required=false) String q,
                               @PageableDefault(size=20, sort="createdAt", direction=Sort.Direction.DESC) Pageable pageable) {
        return recipes.search(q, pageable);
    }

    @GetMapping("/{id}")
    public Recipe get(@PathVariable Long id){
        return recipes.findById(id).orElseThrow();
    }
}