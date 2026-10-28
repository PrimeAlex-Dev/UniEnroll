package com.unienroll.modules.scheduling.entity;

import com.unienroll.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "rooms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_building_room_number", columnNames = {"building", "room_number"})
        },
        indexes = {
                @Index(name = "idx_room_building", columnList = "building")
        }
)
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, length = 30)
    private String roomNumber;

    @Column(name = "building", nullable = false, length = 50)
    private String building;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;
}
