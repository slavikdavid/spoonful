package com.spoonful.spoonful.recipe.web;

import com.spoonful.spoonful.auth.SecurityUtils;
import com.spoonful.spoonful.recipe.dto.RecipeCreateRequest;
import com.spoonful.spoonful.recipe.model.*;
import com.spoonful.spoonful.recipe.repo.RecipeRepository;
import com.spoonful.spoonful.recipe.repo.TagRepository;
import com.spoonful.spoonful.user.User;
import com.spoonful.spoonful.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeRepository recipes;
    private final UserRepository users;
    private final TagRepository tags;

    @PostMapping
    @Transactional
    public Recipe create(@Valid @RequestBody RecipeCreateRequest req){
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        User author = users.findById(uid).orElseThrow();
        Recipe r = new Recipe();
        r.setAuthor(author);
        r.setTitle(req.title());
        r.setDescription(req.description());
        r.setPrepMinutes(req.prepMinutes());
        r.setCookMinutes(req.cookMinutes());

        List<RecipeStep> steps = new ArrayList<>();
        int i=1;
        for (String s : req.steps()) {
            if (s == null || s.isBlank()) continue;
            RecipeStep st = new RecipeStep();
            st.setRecipe(r); st.setStepNo(i++); st.setInstruction(s.trim());
            steps.add(st);
        }
        r.setSteps(steps);

        if (req.tags() != null) {
            for (String raw : req.tags()) {
                if (raw == null || raw.isBlank()) continue;
                String name = raw.trim();
                Tag t = tags.findByNameIgnoreCase(name).orElseGet(() -> tags.save(new Tag(name)));
                r.getTags().add(t);
            }
        }

        return recipes.save(r);
    }
}