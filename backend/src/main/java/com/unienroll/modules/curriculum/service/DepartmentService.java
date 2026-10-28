package com.unienroll.modules.curriculum.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.curriculum.dto.DepartmentRequest;
import com.unienroll.modules.curriculum.dto.DepartmentResponse;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new BusinessException(ErrorCode.ERR_DEPARTMENT_CODE_DUPLICATE, "Mã khoa đã tồn tại: " + request.getCode());
        }

        Department department = Department.builder()
                .code(request.getCode().toUpperCase().trim())
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        Department saved = departmentRepository.save(department);
        log.info("Created department: id={}, code={}", saved.getId(), saved.getCode());
        return mapToResponse(saved);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy khoa với ID: " + id));

        String newCode = request.getCode().toUpperCase().trim();
        if (!department.getCode().equalsIgnoreCase(newCode) && departmentRepository.existsByCode(newCode)) {
            throw new BusinessException(ErrorCode.ERR_DEPARTMENT_CODE_DUPLICATE, "Mã khoa đã tồn tại: " + newCode);
        }

        department.setCode(newCode);
        department.setName(request.getName().trim());
        department.setDescription(request.getDescription());

        Department updated = departmentRepository.save(department);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy khoa với ID: " + id));
        return mapToResponse(department);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy khoa với ID: " + id));

        if (!department.getCourses().isEmpty()) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Không thể xóa khoa đang có môn học liên kết");
        }

        departmentRepository.delete(department);
        log.info("Deleted department id={}", id);
    }

    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .description(department.getDescription())
                .totalCourses(department.getCourses() != null ? department.getCourses().size() : 0)
                .build();
    }
}
