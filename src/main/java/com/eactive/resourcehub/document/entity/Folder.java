package com.eactive.resourcehub.document.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import com.eactive.resourcehub.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "folders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Folder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String folderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FolderType type = FolderType.PERSONAL;

    public static Folder create(User owner, String folderName) {
        Folder folder = new Folder();
        folder.owner = owner;
        folder.folderName = folderName;
        folder.type = FolderType.PERSONAL;
        return folder;
    }

    public static Folder createPublic(User owner, String folderName) {
        Folder folder = new Folder();
        folder.owner = owner;
        folder.folderName = folderName;
        folder.type = FolderType.SHARED_PUBLIC;
        return folder;
    }

    public boolean isPublic() {
        return type == FolderType.SHARED_PUBLIC;
    }
}
