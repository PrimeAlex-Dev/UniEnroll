package com.unienroll.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // General & System Errors
    ERR_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi xảy ra trong hệ thống"),
    ERR_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Dữ liệu yêu cầu không hợp lệ"),
    ERR_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Yêu cầu cần xác thực"),
    ERR_FORBIDDEN(HttpStatus.FORBIDDEN, "Không có quyền truy cập tài nguyên này"),
    ERR_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên yêu cầu"),

    // Module 1: Auth & RBAC Errors
    ERR_AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác"),
    ERR_AUTH_ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa hoặc chưa kích hoạt"),
    ERR_AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại"),
    ERR_AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Token xác thực không hợp lệ hoặc đã hết hạn"),
    ERR_USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Tên đăng nhập hoặc email đã tồn tại trong hệ thống"),
    ERR_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    ERR_INVALID_ROLE(HttpStatus.BAD_REQUEST, "Vai trò người dùng không hợp lệ"),

    // Module 2: Curriculum Errors
    ERR_COURSE_CODE_DUPLICATE(HttpStatus.CONFLICT, "Mã môn học đã tồn tại"),
    ERR_DEPARTMENT_CODE_DUPLICATE(HttpStatus.CONFLICT, "Mã khoa/bộ môn đã tồn tại"),
    ERR_CIRCULAR_PREREQUISITE_DETECTED(HttpStatus.BAD_REQUEST, "Phát hiện phụ thuộc vòng tròn giữa các môn tiên quyết"),

    // Module 3: Scheduling Errors
    ERR_SECTION_CODE_DUPLICATE(HttpStatus.CONFLICT, "Mã lớp học phần đã tồn tại trong học kỳ"),
    ERR_INVALID_SECTION_CAPACITY(HttpStatus.BAD_REQUEST, "Sức chứa lớp học phần không hợp lệ"),
    ERR_ROOM_SCHEDULE_COLLISION(HttpStatus.CONFLICT, "Phòng học đã có lớp đăng ký trong khung giờ này"),
    ERR_LECTURER_SCHEDULE_COLLISION(HttpStatus.CONFLICT, "Giảng viên đã có lịch dạy trong khung giờ này"),
    ERR_ROOM_CAPACITY_INSUFFICIENT(HttpStatus.BAD_REQUEST, "Sức chứa của phòng học nhỏ hơn sức chứa lớp học phần"),
    ERR_CAPACITY_LOWER_THAN_ENROLLED(HttpStatus.BAD_REQUEST, "Không thể giảm sức chứa xuống thấp hơn số sinh viên đã đăng ký"),

    // Module 4: Core Invariants (BR-01 to BR-06)
    ERR_REGISTRATION_WINDOW_CLOSED(HttpStatus.BAD_REQUEST, "Cổng đăng ký học phần hiện đang đóng"),
    ERR_SECTION_CAPACITY_FULL(HttpStatus.CONFLICT, "Lớp học phần đã hết chỗ khả dụng"),
    ERR_TIMETABLE_COLLISION(HttpStatus.CONFLICT, "Lịch học của lớp đăng ký xung đột với lớp học phần khác đã chọn"),
    ERR_PREREQUISITE_NOT_FULFILLED(HttpStatus.BAD_REQUEST, "Sinh viên chưa đạt điều kiện môn học tiên quyết"),
    ERR_CREDIT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Tổng số tín chỉ vượt quá hạn mức tối đa cho phép trong học kỳ"),
    ERR_MINIMUM_CREDIT_UNMET(HttpStatus.BAD_REQUEST, "Tổng số tín chỉ sau khi hủy thấp hơn hạn mức tối thiểu cho phép"),
    ERR_DUPLICATE_COURSE_REGISTRATION(HttpStatus.CONFLICT, "Sinh viên đã đăng ký một lớp học phần khác của môn học này trong kỳ"),

    // Module 5: Records & Reporting Errors
    ERR_ACCESS_DENIED_NOT_ASSIGNED_LECTURER(HttpStatus.FORBIDDEN, "Bạn không phải giảng viên phụ trách lớp học phần này"),
    ERR_ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin đăng ký lớp học phần của sinh viên");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
