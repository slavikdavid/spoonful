package com.spoonful.spoonful.recipe.model;

import com.spoonful.spoonful.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity @Table(name="recipes")
@Getter @Setter @NoArgsConstructor
public class Recipe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="author_id")
    private User author;

    @Column(nullable=false) private String title;
    @Column(columnDefinition="text") private String description;
    private int prepMinutes;
    private int cookMinutes;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy="recipe", cascade=CascadeType.ALL, orphanRemoval=true)
    @OrderBy("stepNo ASC")
    private List<RecipeStep> steps = new ArrayList<>();

    @ManyToMany
    @JoinTable(name="recipe_tags",
            joinColumns=@JoinColumn(name="recipe_id"),
            inverseJoinColumns=@JoinColumn(name="tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "recipe_likes",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )

    private Set<User> likedBy = new HashSet<>();
    public Set<User> getLikedBy() { return likedBy; }
    public List<Comment> getComments() { return comments; }

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder asc, id asc")
    private java.util.List<RecipePhoto> photos = new java.util.ArrayList<>();
}