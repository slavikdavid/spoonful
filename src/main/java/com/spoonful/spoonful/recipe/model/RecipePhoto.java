package com.spoonful.spoonful.recipe.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "recipe_photos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecipePhoto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "file_name", nullable = false) private String fileName;
    @Column(nullable = false) private String url;
    private String contentType;
    @Column(name = "size_bytes") private Long sizeBytes;

    @Column(name = "sort_order", nullable = false) private int sortOrder;
    @Column(name = "is_cover", nullable = false) private boolean cover;

    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
}