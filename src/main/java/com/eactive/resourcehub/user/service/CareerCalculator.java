package com.eactive.resourcehub.user.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CareerCalculator {

    // ── 열거형 ────────────────────────────────────────────────────────────────

    public enum Degree {
        HIGHSCHOOL("고졸"),
        ASSOCIATE("전문학사"),
        BACHELOR("학사"),
        MASTER("석사"),
        DOCTORATE("박사");

        private final String displayName;
        Degree(String d) { this.displayName = d; }
        public String getDisplayName() { return displayName; }
    }

    public enum CertType {
        NONE("없음"),
        ENGINEER("기사"),
        INDUSTRIAL_ENGINEER("산업기사");

        private final String displayName;
        CertType(String d) { this.displayName = d; }
        public String getDisplayName() { return displayName; }
    }

    // ── 결과 레코드 ───────────────────────────────────────────────────────────

    public record DateRange(LocalDate start, LocalDate end) {}

    /** 학위 or 자격증 기준 경력 계산 결과 */
    public record GradeResult(int totalDays, String grade, String basisLabel) {
        public int years()  { return totalDays / 365; }
        public int months() { return (totalDays % 365) / 30; }
        public int days()   { return totalDays % 30; }
        public String displayText() { return formatDays(totalDays); }
        /** 화면 출력용: 학사+22년6개월 */
        public String summary() {
            if (totalDays <= 0) return basisLabel + "+0일";
            return basisLabel + "+" + years() + "년" + (months()>0 ? months()+"개월" : "") + (days()>0 ? days()+"일" : "");
        }
    }

    /** 행별 분석 결과 */
    public record RowResult(
        int no,
        String company,
        LocalDate start,
        LocalDate end,
        int totalDays,
        int educationDays,
        int certDays,
        boolean overlap
    ) {}

    /** 자격 기반 최종 계산 결과 */
    public record CredentialResult(
        GradeResult educationResult,  // 학위 기준 (항상 존재)
        GradeResult certResult,       // 자격증 기준 (자격증 없으면 null)
        String proposedGrade,
        String proposedBasis,
        List<RowResult> rows,
        int rangeCount
    ) {
        /** 저장용 careerMonths 환산 */
        public int saveDays() {
            int certOrd = certResult != null ? gradeOrdinal(certResult.grade()) : -1;
            int eduOrd  = gradeOrdinal(educationResult.grade());
            return certOrd > eduOrd ? certResult.totalDays() : educationResult.totalDays();
        }
        public int saveMonths() { return (int)(saveDays() / 365.0 * 12); }
        public int totalRawDays() { return rows.stream().mapToInt(RowResult::totalDays).sum(); }
        public int rawMonths() { return (int)(totalRawDays() / 365.0 * 12); }
    }

    // ── 등급 기준 테이블 (인덱스: 0=초급,1=중급,2=고급,3=특급, 값: 필요 경력 연수, -1=불가) ──

    private static final Map<Degree, int[]> EDU_THRESH;
    private static final Map<CertType, int[]> CERT_THRESH;
    private static final String[] GRADE_NAMES = {"초급", "중급", "고급", "특급"};

    static {
        EDU_THRESH = new EnumMap<>(Degree.class);
        EDU_THRESH.put(Degree.HIGHSCHOOL, new int[]{ 3, 12, -1, -1});
        EDU_THRESH.put(Degree.ASSOCIATE,  new int[]{ 0,  9, 12, 15});
        EDU_THRESH.put(Degree.BACHELOR,   new int[]{ 0,  6,  9, 12});
        EDU_THRESH.put(Degree.MASTER,     new int[]{ 0,  3,  6,  9});
        EDU_THRESH.put(Degree.DOCTORATE,  new int[]{ 0,  0,  0,  3});

        CERT_THRESH = new EnumMap<>(CertType.class);
        CERT_THRESH.put(CertType.ENGINEER,            new int[]{0,  3,  6,  9});
        CERT_THRESH.put(CertType.INDUSTRIAL_ENGINEER, new int[]{0,  7, 10, 13});
    }

    // ── 핵심 계산 ─────────────────────────────────────────────────────────────

    /**
     * 학위·자격증 기반 등급 산출.
     * gradDate 이후 경력 → 학위 기준, certDate 이후 경력 → 자격증 기준으로 각각 계산.
     */
    public static CredentialResult calculateWithCredentials(
            List<DateRange> rawRanges,
            Degree degree, LocalDate gradDate,
            CertType certType, LocalDate certDate,
            boolean removeOverlap) {

        List<DateRange> valid = rawRanges.stream()
                .filter(r -> r.start() != null && r.end() != null && !r.end().isBefore(r.start()))
                .sorted(Comparator.comparing(DateRange::start))
                .toList();

        Set<Integer> overlapSet = findOverlapIndices(valid);
        List<DateRange> toProcess = removeOverlap ? merge(valid) : valid;

        List<RowResult> rows = new ArrayList<>();
        int totalEduDays = 0, totalCertDays = 0;
        boolean hasCert = certType != null && certType != CertType.NONE && certDate != null;

        for (int i = 0; i < toProcess.size(); i++) {
            DateRange r = toProcess.get(i);
            int totalDays = (int) ChronoUnit.DAYS.between(r.start(), r.end());
            int eduDays  = intersect(r.start(), r.end(), gradDate);
            int certDays = hasCert ? intersect(r.start(), r.end(), certDate) : 0;
            totalEduDays  += eduDays;
            totalCertDays += certDays;
            rows.add(new RowResult(i + 1, null, r.start(), r.end(), totalDays, eduDays, certDays,
                    overlapSet.contains(i)));
        }

        GradeResult eduResult = new GradeResult(totalEduDays,
                determineGrade(totalEduDays, EDU_THRESH.get(degree)),
                degree.getDisplayName());

        GradeResult certResult = hasCert
                ? new GradeResult(totalCertDays,
                        determineGrade(totalCertDays, CERT_THRESH.get(certType)),
                        certType.getDisplayName())
                : null;

        int eduOrd  = gradeOrdinal(eduResult.grade());
        int certOrd = certResult != null ? gradeOrdinal(certResult.grade()) : -1;
        String proposedGrade, proposedBasis;
        if (certOrd > eduOrd) {
            proposedGrade = certResult.grade();
            proposedBasis = certResult.basisLabel();
        } else {
            proposedGrade = eduResult.grade();
            proposedBasis = eduResult.basisLabel();
        }

        return new CredentialResult(eduResult, certResult, proposedGrade, proposedBasis,
                rows, toProcess.size());
    }

    // ── 기존 단순 계산 (하위 호환) ────────────────────────────────────────────

    public record Result(int totalMonths, int totalDays, String displayText, int rangeCount) {
        public int years()        { return totalDays / 365; }
        public int months()       { return (totalDays % 365) / 30; }
        public int days()         { return totalDays % 30; }
        public int remainMonths() { return totalMonths % 12; }
    }

    public static Result calculate(List<DateRange> ranges, boolean removeOverlap) {
        List<DateRange> valid = ranges.stream()
                .filter(r -> r.start() != null && r.end() != null && !r.end().isBefore(r.start()))
                .sorted(Comparator.comparing(DateRange::start))
                .toList();
        if (valid.isEmpty()) return new Result(0, 0, "0일", 0);
        List<DateRange> toSum = removeOverlap ? merge(valid) : valid;
        int totalMonths = toSum.stream()
                .mapToInt(r -> (int) Period.between(r.start(), r.end()).toTotalMonths()).sum();
        int totalDays = toSum.stream()
                .mapToInt(r -> (int) ChronoUnit.DAYS.between(r.start(), r.end())).sum();
        return new Result(totalMonths, totalDays, formatDays(totalDays), toSum.size());
    }

    // ── 유틸리티 ──────────────────────────────────────────────────────────────

    public static String formatDays(int totalDays) {
        if (totalDays <= 0) return "0일";
        int years  = totalDays / 365;
        int rem    = totalDays % 365;
        int months = rem / 30;
        int days   = rem % 30;
        StringBuilder sb = new StringBuilder();
        if (years  > 0) sb.append(years).append("년 ");
        if (months > 0) sb.append(months).append("개월 ");
        if (days   > 0) sb.append(days).append("일");
        if (sb.isEmpty()) sb.append("0일");
        return sb.toString().trim();
    }

    public static String formatMonths(int totalMonths) {
        int years  = totalMonths / 12;
        int months = totalMonths % 12;
        if (years > 0 && months > 0) return years + "년 " + months + "개월";
        if (years > 0) return years + "년";
        return months + "개월";
    }

    /** threshold 이후 기간의 일수만 반환 */
    private static int intersect(LocalDate start, LocalDate end, LocalDate threshold) {
        if (threshold == null || end.isBefore(threshold)) return 0;
        LocalDate s = start.isBefore(threshold) ? threshold : start;
        return (int) ChronoUnit.DAYS.between(s, end);
    }

    /** 인접 행들 중 겹치는 인덱스 집합 */
    private static Set<Integer> findOverlapIndices(List<DateRange> sorted) {
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i + 1 < sorted.size(); i++) {
            if (!sorted.get(i).end().isBefore(sorted.get(i + 1).start())) {
                result.add(i); result.add(i + 1);
            }
        }
        return result;
    }

    /** totalDays 기준으로 thresholds 배열에서 최고 등급 반환 */
    public static String determineGrade(int totalDays, int[] thresholds) {
        double years = totalDays / 365.0;
        String highest = null;
        for (int i = 0; i < 4; i++) {
            if (thresholds[i] >= 0 && years >= thresholds[i]) highest = GRADE_NAMES[i];
        }
        return highest;
    }

    public static int gradeOrdinal(String grade) {
        if (grade == null) return -1;
        for (int i = 0; i < GRADE_NAMES.length; i++) {
            if (GRADE_NAMES[i].equals(grade)) return i;
        }
        return -1;
    }

    private static List<DateRange> merge(List<DateRange> sorted) {
        List<DateRange> merged = new ArrayList<>();
        DateRange cur = sorted.get(0);
        for (int i = 1; i < sorted.size(); i++) {
            DateRange nx = sorted.get(i);
            if (!nx.start().isAfter(cur.end())) {
                cur = new DateRange(cur.start(), cur.end().isAfter(nx.end()) ? cur.end() : nx.end());
            } else { merged.add(cur); cur = nx; }
        }
        merged.add(cur);
        return merged;
    }
}
