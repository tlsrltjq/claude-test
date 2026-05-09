package com.eactive.resourcehub.user.repository;

import com.eactive.resourcehub.user.entity.ColumnViewPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColumnViewPreferenceRepository extends JpaRepository<ColumnViewPreference, Long> {

    List<ColumnViewPreference> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<ColumnViewPreference> findByUserIdAndName(Long userId, String name);

    boolean existsByUserIdAndName(Long userId, String name);
}
