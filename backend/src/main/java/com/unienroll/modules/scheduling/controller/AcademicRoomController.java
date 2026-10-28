package com.unienroll.modules.scheduling.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.scheduling.dto.RoomRequest;
import com.unienroll.modules.scheduling.dto.RoomResponse;
import com.unienroll.modules.scheduling.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic/rooms")
@RequiredArgsConstructor
public class AcademicRoomController {

    private final RoomService roomService;

    /**
     * UC-08: Thêm mới Phòng học
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    /**
     * UC-08: Lấy danh sách tất cả các phòng học
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> response = roomService.getAllRooms();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-08: Xem thông tin phòng học theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
