package com.unienroll.modules.scheduling.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.scheduling.dto.RoomRequest;
import com.unienroll.modules.scheduling.dto.RoomResponse;
import com.unienroll.modules.scheduling.entity.Room;
import com.unienroll.modules.scheduling.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        String building = request.getBuilding().trim();
        String roomNumber = request.getRoomNumber().toUpperCase().trim();

        if (roomRepository.existsByBuildingAndRoomNumber(building, roomNumber)) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED,
                    String.format("Phòng học '%s' tại tòa '%s' đã tồn tại", roomNumber, building));
        }

        Room room = Room.builder()
                .building(building)
                .roomNumber(roomNumber)
                .capacity(request.getCapacity())
                .build();

        Room saved = roomRepository.save(room);
        log.info("Created room: id={}, building={}, roomNumber={}, capacity={}",
                saved.getId(), saved.getBuilding(), saved.getRoomNumber(), saved.getCapacity());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy phòng học với ID: " + id));
        return mapToResponse(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .building(room.getBuilding())
                .roomNumber(room.getRoomNumber())
                .capacity(room.getCapacity())
                .build();
    }
}
