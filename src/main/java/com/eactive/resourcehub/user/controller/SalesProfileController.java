package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.project.service.ProjectAssignmentService;
import com.eactive.resourcehub.team.service.TeamService;
import com.eactive.resourcehub.user.dto.SalesProfileQuery;
import com.eactive.resourcehub.user.entity.ColumnViewPreference;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.service.BundleDownloadService;
import com.eactive.resourcehub.user.service.ColumnViewPreferenceService;
import com.eactive.resourcehub.user.service.SalesProfileExporter;
import com.eactive.resourcehub.user.service.ProfileRow;
import com.eactive.resourcehub.user.service.SalesMemberService;
import com.eactive.resourcehub.user.service.SalesProfileQueryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SalesProfileController {

    private final SalesProfileQueryService profileQueryService;
    private final SalesMemberService salesMemberService;
    private final SalesProfileExporter excelExportService;
    private final BundleDownloadService bundleDownloadService;
    private final ColumnViewPreferenceService presetService;
    private final AuditService auditService;
    private final TeamService teamService;
    private final ObjectMapper objectMapper;
    private final ProjectAssignmentService projectAssignmentService;

    @GetMapping("/sales/profiles")
    public String profiles(@ModelAttribute("query") SalesProfileQuery query,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        List<ProfileRow> rows = profileQueryService.findAllProfiles(query);
        model.addAttribute("rows", rows);
        model.addAttribute("gradeCounts", profileQueryService.getGradeCountsFromRows(rows));
        model.addAttribute("docTypes", DocumentType.values());
        model.addAttribute("positions", Position.values());
        model.addAttribute("teams", teamService.findProjectTeams());
        model.addAttribute("presets", presetService.findByUser(userDetails.getUser().getId()));
        model.addAttribute("currentAssignments", projectAssignmentService.getActiveAssignmentsByUserId());
        model.addAttribute("nextAssignments",    projectAssignmentService.getNextAssignmentsByUserId());
        return "sales/profiles";
    }

    /** GET — 전체 또는 cols 파라미터로 컬럼 지정 (기존 방식 유지) */
    @GetMapping("/sales/profiles/export")
    public ResponseEntity<byte[]> exportExcelGet(
            @ModelAttribute SalesProfileQuery query,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProfileRow> rows = profileQueryService.findAllProfiles(query);
        String[] colsArr = request.getParameterValues("cols");
        Set<String> visibleCols = (colsArr != null && colsArr.length > 0) ? Set.of(colsArr) : Set.of();
        byte[] data = excelExportService.export(rows, visibleCols, query.getCareerDisplay());
        return buildExcelResponse(data, rows.size(), visibleCols, userDetails, request);
    }

    /** POST — selectedIds 체크박스 선택 export */
    @PostMapping("/sales/profiles/export")
    public ResponseEntity<byte[]> exportExcelPost(
            @ModelAttribute SalesProfileQuery query,
            @RequestParam(required = false) List<Long> selectedIds,
            @RequestParam(required = false) String columnsJson,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ProfileRow> all = profileQueryService.findAllProfiles(query);

        // selectedIds 있으면 교집합, 없으면 전체
        List<ProfileRow> rows;
        if (selectedIds != null && !selectedIds.isEmpty()) {
            Set<Long> idSet = Set.copyOf(selectedIds);
            rows = all.stream().filter(r -> idSet.contains(r.getUser().getId())).toList();
        } else {
            rows = all;
        }

        // columnsJson으로 visibleCols 결정
        Set<String> visibleCols = parseColumnsJson(columnsJson);
        byte[] data = excelExportService.export(rows, visibleCols, query.getCareerDisplay());
        return buildExcelResponse(data, rows.size(), visibleCols, userDetails, request);
    }

    /** POST — selectedIds로 ZIP 다운로드 */
    @PostMapping("/sales/profiles/bundle-download")
    public ResponseEntity<byte[]> bundleDownload(
            @ModelAttribute SalesProfileQuery query,
            @RequestParam(required = false) List<Long> selectedIds,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ProfileRow> all = profileQueryService.findAllProfiles(query);
        List<ProfileRow> rows;
        if (selectedIds != null && !selectedIds.isEmpty()) {
            Set<Long> idSet = Set.copyOf(selectedIds);
            rows = all.stream().filter(r -> idSet.contains(r.getUser().getId())).toList();
        } else {
            rows = all;
        }

        byte[] data;
        try {
            data = bundleDownloadService.buildZip(rows);
        } catch (java.io.IOException e) {
            log.warn("번들 ZIP 생성 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        String filename = "투입인력서_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip";
        auditService.log(userDetails.getUser().getId(), AuditActionType.BUNDLE_DOWNLOAD,
                AuditTargetType.USER, null, "selected:" + rows.size() + "명", request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] data, int count, Set<String> cols,
                                                       CustomUserDetails userDetails,
                                                       HttpServletRequest request) {
        String filename = "인력프로필_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        auditService.log(userDetails.getUser().getId(), AuditActionType.EXPORT_PROFILES,
                AuditTargetType.USER, null,
                "selected:" + count + "명 / cols:" + cols.size(), request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private Set<String> parseColumnsJson(String columnsJson) {
        if (columnsJson == null || columnsJson.isBlank() || columnsJson.equals("[]")) return Set.of();
        try {
            List<String> cols = objectMapper.readValue(columnsJson, new TypeReference<>() {});
            return Set.copyOf(cols);
        } catch (Exception e) {
            return Set.of();
        }
    }

    @PostMapping("/sales/profiles/preset")
    public String savePreset(@RequestParam String presetName,
                             @RequestParam(required = false) String columnsJson,
                             @RequestParam(required = false) String sortJson,
                             @RequestParam(required = false, defaultValue = "ymd") String careerDisplay,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes ra) {
        if (presetName == null || presetName.isBlank()) {
            ra.addFlashAttribute("presetError", "프리셋 이름을 입력하세요.");
            return "redirect:/sales/profiles";
        }
        presetService.save(userDetails.getUser().getId(), presetName.trim(),
                columnsJson != null ? columnsJson : "[]",
                sortJson, careerDisplay);
        ra.addFlashAttribute("presetSuccess", "'" + presetName.trim() + "' 프리셋이 저장되었습니다.");
        return "redirect:/sales/profiles";
    }

    @PostMapping("/sales/profiles/preset/{id}/delete")
    public String deletePreset(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes ra) {
        presetService.delete(userDetails.getUser().getId(), id);
        ra.addFlashAttribute("presetSuccess", "프리셋이 삭제되었습니다.");
        return "redirect:/sales/profiles";
    }

    @GetMapping("/sales/employees/{userId}/documents")
    public String employeeDocuments(@PathVariable Long userId, Model model) {
        User member = salesMemberService.findMemberById(userId);
        List<com.eactive.resourcehub.document.entity.Document> documents =
                salesMemberService.findMemberDocuments(userId);
        model.addAttribute("member", member);
        model.addAttribute("documents", documents);
        return "sales/employee-documents";
    }
}
