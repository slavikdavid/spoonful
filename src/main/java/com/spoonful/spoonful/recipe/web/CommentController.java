package com.spoonful.spoonful.recipe.web;

import com.spoonful.spoonful.auth.SecurityUtils;
import com.spoonful.spoonful.recipe.dto.CommentCreateRequest;
import com.spoonful.spoonful.recipe.dto.CommentResponse;
import com.spoonful.spoonful.recipe.model.Comment;
import com.spoonful.spoonful.recipe.model.Recipe;
import com.spoonful.spoonful.recipe.repo.CommentRepository;
import com.spoonful.spoonful.recipe.repo.RecipeRepository;
import com.spoonful.spoonful.user.User;
import com.spoonful.spoonful.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository comments;
    private final RecipeRepository recipes;
    private final UserRepository users;

    @PostMapping("/{recipeId}/comments")
    @Transactional
    public CommentResponse addComment(@PathVariable Long recipeId,
                                      @Valid @RequestBody CommentCreateRequest req) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Recipe recipe = recipes.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User author = users.findById(uid).orElseThrow();

        Comment c = new Comment();
        c.setRecipe(recipe);
        c.setAuthor(author);
        c.setBody(req.body());

        Comment saved = comments.save(c);
        return toDto(saved, uid, true);
    }

    @PostMapping("/{recipeId}/comments/{parentId}/reply")
    @Transactional
    public CommentResponse reply(@PathVariable Long recipeId,
                                 @PathVariable Long parentId,
                                 @Valid @RequestBody CommentCreateRequest req) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Recipe recipe = recipes.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Comment parent = comments.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!Objects.equals(parent.getRecipe().getId(), recipe.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent not in this recipe");
        }

        User author = users.findById(uid).orElseThrow();

        Comment c = new Comment();
        c.setRecipe(recipe);
        c.setAuthor(author);
        c.setParent(parent);
        c.setBody(req.body());

        Comment saved = comments.save(c);
        return toDto(saved, uid, true);
    }

    @GetMapping("/{recipeId}/comments")
    public List<CommentResponse> listComments(@PathVariable Long recipeId) {
        Long viewerId = SecurityUtils.currentUserId(); // may be null
        Recipe recipe = recipes.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return comments.findByRecipeIdAndParentIsNullOrderByCreatedAtAsc(recipe.getId())
                .stream().map(c -> toDto(c, viewerId, true)).toList();
    }

    @PutMapping("/comments/{commentId}/likes")
    @Transactional
    public Map<String, Object> likeComment(@PathVariable Long commentId) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Comment c = comments.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User me = users.findById(uid).orElseThrow();

        c.getLikedBy().add(me);
        return Map.of("liked", true, "likes", c.getLikedBy().size());
    }

    @DeleteMapping("/comments/{commentId}/likes")
    @Transactional
    public Map<String, Object> unlikeComment(@PathVariable Long commentId) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Comment c = comments.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User me = users.findById(uid).orElseThrow();

        c.getLikedBy().remove(me);
        return Map.of("liked", false, "likes", c.getLikedBy().size());
    }

    private CommentResponse toDto(Comment c, Long viewerId, boolean includeReplies) {
        List<CommentResponse> replies = includeReplies
                ? c.getChildren().stream()
                .map(ch -> toDto(ch, viewerId, true))
                .toList()
                : Collections.emptyList();

        int likeCount = (c.getLikedBy() == null) ? 0 : c.getLikedBy().size();
        boolean likedByMe = viewerId != null && c.getLikedBy() != null &&
                c.getLikedBy().stream().anyMatch(u -> Objects.equals(u.getId(), viewerId));

        return new CommentResponse(
                c.getId(),
                c.getAuthor().getId(),
                c.getAuthor().getDisplayName(),
                c.getBody(),
                c.getCreatedAt(),
                likeCount,
                likedByMe,
                replies
        );
    }
}
