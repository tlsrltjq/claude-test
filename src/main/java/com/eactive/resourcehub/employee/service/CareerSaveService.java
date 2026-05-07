package com.eactive.resourcehub.employee.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.employee.entity.EmployeeProfile;
import com.eactive.resourcehub.employee.repository.EmployeeProfileRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CareerSaveService {

    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final AuditService auditService;

    @Transactional
    public void saveCareer(Long targetUserId, int careerMonths, int careerTotalDays,
                           String developerGrade, Long actorId, HttpServletRequest request) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        EmployeeProfile profile = employeeProfileRepository.findByUserId(targetUserId)
                .orElseGet(() -> employeeProfileRepository.save(EmployeeProfile.create(target)));

        profile.updateCareer(
                developerGrade != null && !developerGrade.isBlank() ? developerGrade.trim() : null,
                careerMonths,
                careerTotalDays
        );

        auditService.log(actorId, AuditActionType.UPDATE_CAREER_PROFILE,
                AuditTargetType.USER, targetUserId,
                "경력 " + careerMonths + "개월(" + careerTotalDays + "일), 등급: " + developerGrade, request);
    }
}
