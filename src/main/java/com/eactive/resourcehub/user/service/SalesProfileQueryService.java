package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.employee.entity.EmployeeProfile;
import com.eactive.resourcehub.employee.repository.EmployeeProfileRepository;
import com.eactive.resourcehub.user.dto.SalesProfileQuery;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesProfileQueryService {

    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    public List<ProfileRow> findAllProfiles(SalesProfileQuery query) {
        // 1. 활성 사용자 전체 (팀 페치)
        List<User> users = userRepository.findByStatusWithTeam(UserStatus.ACTIVE);
        if (users.isEmpty()) return Collections.emptyList();

        List<Long> userIds = users.stream().map(User::getId).toList();

        // 2. 폴더 배치 조회 (userId → folderId)
        Map<Long, Long> userToFolder = folderRepository.findByOwnerIdIn(userIds)
                .stream().collect(Collectors.toMap(f -> f.getOwner().getId(), Folder::getId));

        // 3. 문서 배치 조회 (folderId → documents with currentVersion)
        Map<Long, Map<DocumentType, DocumentVersion>> userDocMap = new HashMap<>();
        List<Long> folderIds = List.copyOf(userToFolder.values());
        if (!folderIds.isEmpty()) {
            List<Document> docs = documentRepository.findActiveWithVersionByFolderIds(folderIds);
            Map<Long, Long> folderToUser = userToFolder.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            for (Document doc : docs) {
                if (doc.getCurrentVersion() == null) continue;
                Long userId = folderToUser.get(doc.getFolder().getId());
                if (userId == null) continue;
                userDocMap.computeIfAbsent(userId, k -> new EnumMap<>(DocumentType.class))
                        .putIfAbsent(doc.getDocumentType(), doc.getCurrentVersion());
            }
        }

        // 4. EmployeeProfile 배치 조회
        Map<Long, EmployeeProfile> profileMap = employeeProfileRepository.findByUserIdIn(userIds)
                .stream().collect(Collectors.toMap(ep -> ep.getUser().getId(), ep -> ep));

        // 5. 조합 + 필터 + 정렬
        List<ProfileRow> rows = users.stream()
                .map(u -> new ProfileRow(u, profileMap.get(u.getId()),
                        userDocMap.getOrDefault(u.getId(), Collections.emptyMap())))
                .filter(r -> matches(r, query))
                .collect(Collectors.toCollection(ArrayList::new));

        rows.sort(buildComparator(query));
        return rows;
    }

    private boolean matches(ProfileRow row, SalesProfileQuery query) {
        if (query.getQ() != null && !query.getQ().isBlank()) {
            String q = query.getQ().toLowerCase();
            String name = row.getUser().getName() != null ? row.getUser().getName().toLowerCase() : "";
            String email = row.getUser().getEmail() != null ? row.getUser().getEmail().toLowerCase() : "";
            if (!name.contains(q) && !email.contains(q)) return false;
        }
        Position pos = query.positionEnum();
        if (pos != null && pos != row.getUser().getPosition()) return false;
        if (query.getDeveloperGrade() != null && !query.getDeveloperGrade().isBlank()) {
            String grade = row.getDeveloperGrade();
            if (grade == null || !grade.equalsIgnoreCase(query.getDeveloperGrade().trim())) return false;
        }
        return true;
    }

    private Comparator<ProfileRow> buildComparator(SalesProfileQuery query) {
        String sort = query.getSort() != null ? query.getSort() : "name";
        boolean asc = query.isSortAsc();

        Comparator<ProfileRow> comp;
        if ("position".equals(sort)) {
            comp = Comparator.comparingInt(r ->
                    r.getUser().getPosition() != null ? r.getUser().getPosition().ordinal() : Integer.MAX_VALUE);
        } else if ("career".equals(sort)) {
            comp = Comparator.comparingInt(ProfileRow::getCareerMonths);
        } else if ("age".equals(sort)) {
            comp = Comparator.comparingInt(ProfileRow::getAge);
        } else {
            comp = Comparator.comparing(r -> r.getUser().getName() != null ? r.getUser().getName() : "");
        }

        return asc ? comp : comp.reversed();
    }
}
