package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.user.entity.ColumnViewPreference;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.ColumnViewPreferenceRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColumnViewPreferenceService {

    private final ColumnViewPreferenceRepository repo;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ColumnViewPreference> findByUser(Long userId) {
        return repo.findByUserIdOrderByCreatedAtAsc(userId);
    }

    @Transactional
    public ColumnViewPreference save(Long userId, String name, String columnsJson,
                                     String sortJson, String careerDisplay) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return repo.findByUserIdAndName(userId, name)
                .map(existing -> {
                    existing.update(columnsJson, sortJson, careerDisplay);
                    return existing;
                })
                .orElseGet(() -> repo.save(
                        ColumnViewPreference.create(user, name, columnsJson, sortJson, careerDisplay, false)));
    }

    @Transactional
    public void delete(Long userId, Long presetId) {
        ColumnViewPreference pref = repo.findById(presetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!pref.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        repo.delete(pref);
    }
}
