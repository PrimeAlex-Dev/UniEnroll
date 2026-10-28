package com.unienroll.modules.scheduling.repository;

import com.unienroll.modules.scheduling.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByBuildingAndRoomNumber(String building, String roomNumber);

    Optional<Room> findByRoomNumber(String roomNumber);

    boolean existsByBuildingAndRoomNumber(String building, String roomNumber);

    boolean existsByRoomNumber(String roomNumber);
}
