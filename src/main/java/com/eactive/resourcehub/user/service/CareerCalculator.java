package com.eactive.resourcehub.user.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CareerCalculator {

    public record DateRange(LocalDate start, LocalDate end) {}

    public record Result(int totalMonths, String displayText, int rangeCount) {
        public int years() { return totalMonths / 12; }
        public int remainMonths() { return totalMonths % 12; }
    }

    public static Result calculate(List<DateRange> ranges, boolean removeOverlap) {
        List<DateRange> valid = ranges.stream()
                .filter(r -> r.start() != null && r.end() != null && !r.end().isBefore(r.start()))
                .sorted(Comparator.comparing(DateRange::start))
                .toList();

        if (valid.isEmpty()) return new Result(0, "0개월", 0);

        List<DateRange> toSum = removeOverlap ? merge(valid) : valid;

        int totalMonths = toSum.stream()
                .mapToInt(r -> (int) Period.between(r.start(), r.end()).toTotalMonths())
                .sum();

        return new Result(totalMonths, formatMonths(totalMonths), toSum.size());
    }

    public static String formatMonths(int totalMonths) {
        int years = totalMonths / 12;
        int months = totalMonths % 12;
        if (years > 0 && months > 0) return years + "년 " + months + "개월";
        if (years > 0) return years + "년";
        return months + "개월";
    }

    private static List<DateRange> merge(List<DateRange> sorted) {
        List<DateRange> merged = new ArrayList<>();
        DateRange current = sorted.get(0);
        for (int i = 1; i < sorted.size(); i++) {
            DateRange next = sorted.get(i);
            if (!next.start().isAfter(current.end())) {
                LocalDate newEnd = current.end().isAfter(next.end()) ? current.end() : next.end();
                current = new DateRange(current.start(), newEnd);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }
}
