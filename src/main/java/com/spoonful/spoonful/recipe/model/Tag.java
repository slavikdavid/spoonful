package com.spoonful.spoonful.recipe.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="tags")
@Getter @Setter @NoArgsConstructor
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=64)
    private String name;

    public Tag(String name) { this.name = name; }
}