package com.spoonful.spoonful.recipe.dto;

import java.time.Instant;
import java.util.List;

public record CommentResponse(
        Long id,
        Long authorId,
        String authorName,
        String body,
        Instant createdAt,
        int likeCount,
        boolean likedByMe,
        List<CommentResponse> replies
) {}