package com.unienroll.modules.auth.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    ROLE_STUDENT,
    ROLE_LECTURER,
    ROLE_STAFF,
    ROLE_ADMIN;

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.trim().toUpperCase();
        if (clean.equals("STUDENT") || clean.equals("ROLE_STUDENT")) return ROLE_STUDENT;
        if (clean.equals("LECTURER") || clean.equals("ROLE_LECTURER")) return ROLE_LECTURER;
        if (clean.equals("STAFF") || clean.equals("ROLE_STAFF") || clean.equals("ACADEMIC_STAFF") || clean.equals("ROLE_ACADEMIC_STAFF")) return ROLE_STAFF;
        if (clean.equals("ADMIN") || clean.equals("ROLE_ADMIN")) return ROLE_ADMIN;
        return Role.valueOf(clean);
    }
}
