package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.employee.entity.EmployeeProfile;
import com.eactive.resourcehub.user.entity.User;
import lombok.Getter;

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

@Getter
public class ProfileRow {

    private final User user;
    private final EmployeeProfile profile;
    private final Map<DocumentType, DocumentVersion> documents;

    public ProfileRow(User user, EmployeeProfile profile, Map<DocumentType, DocumentVersion> documents) {
        this.user = user;
        this.profile = profile;
        this.documents = documents;
    }

    public int getAge() {
        if (user.getBirthDate() == null) return 0;
        return Period.between(user.getBirthDate(), LocalDate.now()).getYears();
    }

    public String getDeveloperGrade() {
        return profile != null ? profile.getDeveloperGrade() : null;
    }

    public int getCareerTotalDays() {
        return profile != null ? profile.getCareerTotalDays() : 0;
    }

    public int getCareerMonths() {
        return profile != null ? profile.getCareerMonths() : 0;
    }

    /** careerDisplay="ymd" 용: "N년 N개월" */
    public String getCareerYmd() {
        int days = getCareerTotalDays();
        if (days <= 0) return null;
        int years  = days / 365;
        int months = (days % 365) / 30;
        StringBuilder sb = new StringBuilder();
        if (years  > 0) sb.append(years).append("년 ");
        if (months > 0) sb.append(months).append("개월");
        return sb.toString().trim();
    }

    /** careerDisplay="m" 용: N개월 */
    public int getCareerMonthsFromDays() {
        return getCareerTotalDays() / 30;
    }

    public DocumentVersion getDoc(DocumentType type) {
        return documents.get(type);
    }
}
