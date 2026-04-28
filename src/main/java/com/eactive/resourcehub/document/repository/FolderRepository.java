package com.eactive.resourcehub.document.repository;

import com.eactive.resourcehub.document.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    Optional<Folder> findByOwnerId(Long ownerId);

    List<Folder> findByOwnerTeamId(Long teamId);

    boolean existsByOwnerId(Long ownerId);
}
