package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.employee.service.CareerSaveService;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import com.eactive.resourcehub.user.service.CareerCalculator;
import com.eactive.resourcehub.user.service.CareerCalculator.Degree;
import com.eactive.resourcehub.user.service.CareerCalculator.CertType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CareerCalculatorController {

    private final UserRepository userRepository;
    private final CareerSaveService careerSaveService;

    private static final List<String> GRADES = List.of("특급", "고급", "중급", "초급");

    @GetMapping("/sales/career-calculator")
    public String form(@RequestParam(required = false) Long targetUserId, Model model) {
        loadCommon(model, targetUserId, null, null, null, null, false);
        model.addAttribute("startDates", List.of(""));
        model.addAttribute("endDates",   List.of(""));
        model.addAttribute("companies",  List.of(""));
        return "sales/career-calculator";
    }

    @PostMapping("/sales/career-calculator")
    public String calculate(
            @RequestParam(required = false) Long     targetUserId,
            @RequestParam(required = false) String   degree,
            @RequestParam(required = false) String   gradDate,
            @RequestParam(required = false) String   certType,
            @RequestParam(required = false) String   certDate,
            @RequestParam(required = false) String   developerGrade,
            @RequestParam(name = "companies",  required = false) List<String> companies,
            @RequestParam(name = "startDates", required = false) List<String> startDates,
            @RequestParam(name = "endDates",   required = false) List<String> endDates,
            @RequestParam(name = "removeOverlap", required = false, defaultValue = "false") boolean removeOverlap,
            Model model) {

        if (companies  == null) companies  = new ArrayList<>();
        if (startDates == null) startDates = new ArrayList<>();
        if (endDates   == null) endDates   = new ArrayList<>();

        int rowCount = Math.max(1, startDates.size());
        List<String> paddedStarts     = padTo(startDates, rowCount);
        List<String> paddedEnds       = padTo(endDates, rowCount);
        List<String> paddedCompanies  = padTo(companies, rowCount);

        model.addAttribute("startDates", paddedStarts);
        model.addAttribute("endDates",   paddedEnds);
        model.addAttribute("companies",  paddedCompanies);
        model.addAttribute("removeOverlap", removeOverlap);
        model.addAttribute("degree",      degree);
        model.addAttribute("gradDate",    gradDate);
        model.addAttribute("certType",    certType);
        model.addAttribute("certDate",    certDate);
        model.addAttribute("developerGrade", developerGrade);
        loadCommon(model, targetUserId, degree, gradDate, certType, certDate, removeOverlap);

        // 학위/졸업일 필수 검사
        Degree degreeEnum = parseEnum(Degree.class, degree);
        LocalDate gradDateParsed = parseDate(gradDate);
        if (degreeEnum == null || gradDateParsed == null) {
            model.addAttribute("inputError", "학위와 졸업일자는 필수입니다.");
            return "sales/career-calculator";
        }

        CertType certTypeEnum = parseEnum(CertType.class, certType);
        if (certTypeEnum == null) certTypeEnum = CertType.NONE;
        LocalDate certDateParsed = (certTypeEnum != CertType.NONE) ? parseDate(certDate) : null;
        if (certTypeEnum != CertType.NONE && certDateParsed == null) {
            model.addAttribute("inputError", "자격증 선택 시 취득일자를 입력해야 합니다.");
            return "sales/career-calculator";
        }

        List<String> errors = new ArrayList<>();
        List<CareerCalculator.DateRange> ranges = new ArrayList<>();
        int size = Math.min(paddedStarts.size(), paddedEnds.size());
        for (int i = 0; i < size; i++) {
            String s = paddedStarts.get(i), e = paddedEnds.get(i);
            if (s == null || s.isBlank() || e == null || e.isBlank()) continue;
            try {
                LocalDate start = LocalDate.parse(s), end = LocalDate.parse(e);
                if (end.isBefore(start)) { errors.add((i+1) + "번 기간: 종료일이 시작일보다 앞섭니다."); continue; }
                ranges.add(new CareerCalculator.DateRange(start, end));
            } catch (DateTimeParseException ex) {
                errors.add((i+1) + "번 기간: 날짜 형식이 올바르지 않습니다.");
            }
        }

        if (!errors.isEmpty()) { model.addAttribute("errors", errors); }

        if (!ranges.isEmpty()) {
            // 회사명을 RowResult에 주입
            CareerCalculator.CredentialResult rawResult =
                    CareerCalculator.calculateWithCredentials(ranges, degreeEnum, gradDateParsed,
                            certTypeEnum, certDateParsed, removeOverlap);

            // 회사명 병합
            List<CareerCalculator.RowResult> rowsWithCompany = new ArrayList<>();
            for (int i = 0; i < rawResult.rows().size(); i++) {
                CareerCalculator.RowResult r = rawResult.rows().get(i);
                String company = (i < paddedCompanies.size()) ? paddedCompanies.get(i) : "";
                rowsWithCompany.add(new CareerCalculator.RowResult(
                        r.no(), company, r.start(), r.end(),
                        r.totalDays(), r.educationDays(), r.certDays(), r.overlap()));
            }

            CareerCalculator.CredentialResult result = new CareerCalculator.CredentialResult(
                    rawResult.educationResult(), rawResult.certResult(),
                    rawResult.proposedGrade(), rawResult.proposedBasis(),
                    rowsWithCompany, rawResult.rangeCount());

            model.addAttribute("result", result);
        } else if (errors.isEmpty()) {
            model.addAttribute("infoMessage", "경력 기간을 입력해 주세요.");
        }

        return "sales/career-calculator";
    }

    @PostMapping("/sales/career-calculator/save")
    public String save(
            @RequestParam Long   targetUserId,
            @RequestParam int    careerMonths,
            @RequestParam(defaultValue = "0") int careerTotalDays,
            @RequestParam(required = false) String developerGrade,
            @AuthenticationPrincipal CustomUserDetails actor,
            HttpServletRequest request,
            RedirectAttributes ra) {
        try {
            careerSaveService.saveCareer(targetUserId, careerMonths, careerTotalDays,
                    developerGrade, actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "프로필이 저장되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "저장 중 오류: " + e.getMessage());
        }
        return "redirect:/sales/career-calculator?targetUserId=" + targetUserId;
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private void loadCommon(Model model, Long targetUserId, String degree, String gradDate,
                            String certType, String certDate, boolean removeOverlap) {
        model.addAttribute("members",
                userRepository.findByStatusWithTeam(UserStatus.ACTIVE).stream()
                        .filter(u -> u.getRole() != UserRole.ADMIN)
                        .sorted(java.util.Comparator.comparing(u ->
                                u.getTeam() != null ? u.getTeam().getName() + u.getName() : u.getName()))
                        .toList());
        model.addAttribute("targetUserId",  targetUserId);
        model.addAttribute("degree",        degree);
        model.addAttribute("gradDate",      gradDate);
        model.addAttribute("certType",      certType);
        model.addAttribute("certDate",      certDate);
        model.addAttribute("removeOverlap", removeOverlap);
        model.addAttribute("degrees",       Degree.values());
        model.addAttribute("grades",        GRADES);
    }

    private static List<String> padTo(List<String> list, int minSize) {
        List<String> r = new ArrayList<>(list);
        while (r.size() < minSize) r.add("");
        return r;
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); } catch (DateTimeParseException e) { return null; }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> cls, String s) {
        if (s == null || s.isBlank()) return null;
        try { return Enum.valueOf(cls, s.trim()); } catch (IllegalArgumentException e) { return null; }
    }
}
