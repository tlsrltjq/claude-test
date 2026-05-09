package com.eactive.resourcehub.document.repository;

import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    Optional<Folder> findByOwnerId(Long ownerId);

    Optional<Folder> findByOwnerIdAndType(Long ownerId, FolderType type);

    boolean existsByOwnerIdAndType(Long ownerId, FolderType type);

    List<Folder> findByOwnerTeamId(Long teamId);

    boolean existsByOwnerId(Long ownerId);

    @Query("SELECT f FROM Folder f JOIN FETCH f.owner u LEFT JOIN FETCH u.team WHERE f.id = :id")
    Optional<Folder> findByIdWithOwner(@Param("id") Long id);

    @Query("SELECT f FROM Folder f JOIN FETCH f.owner u LEFT JOIN FETCH u.team WHERE f.id IN :ids")
    List<Folder> findByIdInWithOwner(@Param("ids") Collection<Long> ids);

    @Query("SELECT f FROM Folder f JOIN FETCH f.owner u LEFT JOIN FETCH u.team")
    List<Folder> findAllWithOwner();

    @Query("SELECT f FROM Folder f JOIN FETCH f.owner u LEFT JOIN FETCH u.team WHERE u.team.id = :teamId")
    List<Folder> findByOwnerTeamIdWithOwner(@Param("teamId") Long teamId);

    @Query("SELECT f FROM Folder f WHERE f.owner.id IN :ownerIds")
    List<Folder> findByOwnerIdIn(@Param("ownerIds") List<Long> ownerIds);

    @Query("SELECT f FROM Folder f WHERE f.owner.id IN :ownerIds AND f.type = :type")
    List<Folder> findByOwnerIdInAndType(@Param("ownerIds") List<Long> ownerIds, @Param("type") FolderType type);

    Optional<Folder> findFirstByType(FolderType type);

    boolean existsByType(FolderType type);
}
