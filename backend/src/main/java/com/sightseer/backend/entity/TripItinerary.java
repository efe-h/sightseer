package com.sightseer.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "trip_itinerary", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "trip_id", "visit_date", "visit_order" })
})
@Getter
@Setter
public class TripItinerary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trip_id", nullable = false, foreignKey = @ForeignKey(name = "fk_itinerary_trip"))
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attraction_id", nullable = false, foreignKey = @ForeignKey(name = "fk_itinerary_attraction"))
    private Attraction attraction;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "recommendation_score", precision = 4, scale = 2)
    private BigDecimal recommendationScore;
}
