package com.sightseer.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attraction_category_scores", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "attraction_id", "category_id" })
})
@Getter
@Setter
public class AttractionCategoryScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attraction_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attraction_score_attraction"))
    private Attraction attraction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attraction_score_category"))
    private Category category;

    @Column(name = "score", nullable = false)
    private Short score;
}
