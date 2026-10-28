package com.unienroll.modules.scheduling.entity;

import com.unienroll.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "schedule_slots", indexes = {
        @Index(name = "idx_slot_section", columnList = "section_id"),
        @Index(name = "idx_slot_room", columnList = "room_id"),
        @Index(name = "idx_slot_day", columnList = "day_of_week")
})
public class ScheduleSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_period", nullable = false)
    private Integer startPeriod;

    @Column(name = "end_period", nullable = false)
    private Integer endPeriod;
}
