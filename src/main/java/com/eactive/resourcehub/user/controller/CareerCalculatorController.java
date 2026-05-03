package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.user.service.CareerCalculator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CareerCalculatorController {

    @GetMapping("/sales/career-calculator")
    public String form(Model model) {
        model.addAttribute("startDates", List.of(""));
        model.addAttribute("endDates", List.of(""));
        model.addAttribute("removeOverlap", false);
        return "sales/career-calculator";
    }

    @PostMapping("/sales/career-calculator")
    public String calculate(
            @RequestParam(name = "startDates", required = false) List<String> startDates,
            @RequestParam(name = "endDates", required = false) List<String> endDates,
            @RequestParam(name = "removeOverlap", required = false, defaultValue = "false") boolean removeOverlap,
            Model model) {

        if (startDates == null) startDates = new ArrayList<>();
        if (endDates == null) endDates = new ArrayList<>();

        int rowCount = Math.max(1, startDates.size());
        model.addAttribute("startDates", padTo(startDates, rowCount));
        model.addAttribute("endDates", padTo(endDates, rowCount));
        model.addAttribute("removeOverlap", removeOverlap);

        List<String> errors = new ArrayList<>();
        List<CareerCalculator.DateRange> ranges = new ArrayList<>();

        int size = Math.min(startDates.size(), endDates.size());
        for (int i = 0; i < size; i++) {
            String s = startDates.get(i);
            String e = endDates.get(i);
            if (s == null || s.isBlank() || e == null || e.isBlank()) continue;
            try {
                LocalDate start = LocalDate.parse(s);
                LocalDate end = LocalDate.parse(e);
                if (end.isBefore(start)) {
                    errors.add((i + 1) + "번 기간: 종료일이 시작일보다 앞설 수 없습니다.");
                    continue;
                }
                ranges.add(new CareerCalculator.DateRange(start, end));
            } catch (DateTimeParseException ex) {
                errors.add((i + 1) + "번 기간: 날짜 형식이 올바르지 않습니다.");
            }
        }

        if (!errors.isEmpty()) model.addAttribute("errors", errors);

        if (!ranges.isEmpty()) {
            model.addAttribute("result", CareerCalculator.calculate(ranges, removeOverlap));
        } else if (errors.isEmpty()) {
            model.addAttribute("infoMessage", "계산할 기간을 입력해 주세요.");
        }

        return "sales/career-calculator";
    }

    private static List<String> padTo(List<String> list, int minSize) {
        List<String> result = new ArrayList<>(list);
        while (result.size() < minSize) result.add("");
        return result;
    }
}
