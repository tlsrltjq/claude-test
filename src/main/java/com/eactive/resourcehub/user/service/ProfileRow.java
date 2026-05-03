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

    public int getCareerMonths() {
        return profile != null ? profile.getCareerMonths() : 0;
    }

    public int getCareerYears() {
        return getCareerMonths() / 12;
    }

    public int getCareerRemainMonths() {
        return getCareerMonths() % 12;
    }

    public DocumentVersion getDoc(DocumentType type) {
        return documents.get(type);
    }
}
