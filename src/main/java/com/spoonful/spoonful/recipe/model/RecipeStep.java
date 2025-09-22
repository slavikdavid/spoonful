package com.spoonful.spoonful.recipe.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="recipe_steps")
@Getter @Setter @NoArgsConstructor
public class RecipeStep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="recipe_id")
    private Recipe recipe;

    @Column(nullable=false) private int stepNo;
    @Column(columnDefinition="text", nullable=false) private String instruction;
}