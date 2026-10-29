package com.unienroll.modules.scheduling.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduleCollisionDetectorTest {

    private CourseSectionService sectionService;

    @BeforeEach
    void setUp() {
        sectionService = new CourseSectionService(null, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("Hai khoảng tiết tiếp giáp một tiết chung (Tiết 1-3 và Tiết 3-5) phải được coi là giao thoa (Overlap)")
    void shouldDetectOverlapAtBoundary() {
        assertTrue(sectionService.isTimeOverlap(1, 3, 3, 5));
        assertTrue(sectionService.isTimeOverlap(3, 5, 1, 3));
    }

    @Test
    @DisplayName("Hai khoảng tiết hoàn toàn tách biệt (Tiết 1-3 và Tiết 4-6) không được coi là giao thoa")
    void shouldNotDetectOverlapForDisjointIntervals() {
        assertFalse(sectionService.isTimeOverlap(1, 3, 4, 6));
        assertFalse(sectionService.isTimeOverlap(4, 6, 1, 3));
    }

    @Test
    @DisplayName("Khoảng tiết này bao trùm khoảng tiết kia (Tiết 1-6 và Tiết 2-4) là giao thoa")
    void shouldDetectOverlapWhenOneContainsAnother() {
        assertTrue(sectionService.isTimeOverlap(1, 6, 2, 4));
        assertTrue(sectionService.isTimeOverlap(2, 4, 1, 6));
    }

    @Test
    @DisplayName("Trùng lặp một tiết duy nhất (Tiết 4-4 và Tiết 4-4) là giao thoa")
    void shouldDetectOverlapForSinglePeriod() {
        assertTrue(sectionService.isTimeOverlap(4, 4, 4, 4));
    }
}
