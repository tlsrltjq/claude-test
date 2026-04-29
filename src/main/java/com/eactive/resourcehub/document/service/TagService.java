package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.Tag;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    @Transactional
    public void addTag(Long documentId, String tagName, Long requesterId) {
        Document document = getOwnedDocument(documentId, requesterId);
        String normalized = tagName.trim().toLowerCase();
        if (normalized.isEmpty() || normalized.length() > 50) {
            throw new IllegalArgumentException("태그 이름은 1~50자여야 합니다.");
        }
        Tag tag = tagRepository.findByName(normalized)
                .orElseGet(() -> tagRepository.save(Tag.of(normalized)));
        document.addTag(tag);
    }

    @Transactional
    public void removeTag(Long documentId, Long tagId, Long requesterId) {
        Document document = getOwnedDocument(documentId, requesterId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다."));
        document.removeTag(tag);
    }

    private Document getOwnedDocument(Long documentId, Long requesterId) {
        Document document = documentRepository.findByIdForDetailWithTags(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));
        if (!document.getFolder().getOwner().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return document;
    }
}
