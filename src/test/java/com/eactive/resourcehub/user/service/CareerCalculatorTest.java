package com.eactive.resourcehub.user.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CareerCalculatorTest {

    @Test
    void 단순합산_중복없음() {
        var ranges = List.of(
                new CareerCalculator.DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1)),
                new CareerCalculator.DateRange(LocalDate.of(2022, 6, 1), LocalDate.of(2023, 6, 1))
        );
        var result = CareerCalculator.calculate(ranges, false);
        assertEquals(24, result.totalMonths()); // 12 + 12
        assertEquals(2, result.rangeCount());
    }

    @Test
    void 중복제거_겹치는구간() {
        var ranges = List.of(
                new CareerCalculator.DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 6, 1)),
                new CareerCalculator.DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2022, 1, 1))
        );
        var result = CareerCalculator.calculate(ranges, true);
        // merged: 2020-01-01 ~ 2022-01-01 = 24 months
        assertEquals(24, result.totalMonths());
        assertEquals(1, result.rangeCount());
    }

    @Test
    void 중복없이_연속구간() {
        var ranges = List.of(
                new CareerCalculator.DateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 7, 1)),
                new CareerCalculator.DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 7, 1))
        );
        var result = CareerCalculator.calculate(ranges, true);
        assertEquals(12, result.totalMonths()); // 6 + 6, no overlap
        assertEquals(2, result.rangeCount());
    }

    @Test
    void 빈입력_결과0() {
        var result = CareerCalculator.calculate(List.of(), false);
        assertEquals(0, result.totalMonths());
    }

    @Test
    void 포맷_년개월() {
        assertEquals("1년 6개월", CareerCalculator.formatMonths(18));
        assertEquals("2년", CareerCalculator.formatMonths(24));
        assertEquals("3개월", CareerCalculator.formatMonths(3));
    }

    @Test
    void 종료일이_시작일보다_앞선경우_무시() {
        var ranges = List.of(
                new CareerCalculator.DateRange(LocalDate.of(2022, 6, 1), LocalDate.of(2020, 1, 1)) // invalid
        );
        var result = CareerCalculator.calculate(ranges, false);
        assertEquals(0, result.totalMonths());
    }
}
