package com.internship.tool.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.internship.tool.entity.FileAttachment;
import com.internship.tool.entity.RegulatoryChange;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.sql.init.mode=never",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class RepositoryDataJpaTest {

    @Autowired
    private RegulatoryChangeRepository regulatoryChangeRepository;

    @Autowired
    private FileAttachmentRepository fileAttachmentRepository;

    @Test
    void regulatoryChangeRepository_shouldFindByTitleAndSourceIgnoringCase() {
        RegulatoryChange change = new RegulatoryChange();
        change.setTitle("RBI Circular");
        change.setDescription("Updated KYC rules");
        change.setSource("RBI");
        change.setJurisdiction("India");
        change.setCategory("Compliance");
        change.setStatus("NEW");
        change.setPriority("HIGH");
        change.setPublishedDate(LocalDate.now().minusDays(1));
        change.setEffectiveDate(LocalDate.now().plusDays(10));
        change.setCreatedAt(LocalDateTime.now().minusHours(1));
        change.setUpdatedAt(LocalDateTime.now());
        regulatoryChangeRepository.save(change);

        boolean exists = regulatoryChangeRepository
                .existsByTitleIgnoreCaseAndSourceIgnoreCase("rbi circular", "rbi");

        assertTrue(exists);
    }

    @Test
    void regulatoryChangeRepository_shouldRespectIdNotCheck() {
        RegulatoryChange first = new RegulatoryChange();
        first.setTitle("SEBI Update");
        first.setDescription("Disclosure rules updated");
        first.setSource("SEBI");
        first.setJurisdiction("India");
        first.setCategory("Disclosure");
        first.setStatus("NEW");
        first.setPriority("MEDIUM");
        first.setPublishedDate(LocalDate.now().minusDays(2));
        first.setEffectiveDate(LocalDate.now().plusDays(5));
        first.setCreatedAt(LocalDateTime.now().minusHours(1));
        first.setUpdatedAt(LocalDateTime.now());
        RegulatoryChange saved = regulatoryChangeRepository.save(first);

        boolean existsForDifferentId = regulatoryChangeRepository
                .existsByTitleIgnoreCaseAndSourceIgnoreCaseAndIdNot("sebi update", "sebi", saved.getId() + 100);
        boolean existsForSameId = regulatoryChangeRepository
                .existsByTitleIgnoreCaseAndSourceIgnoreCaseAndIdNot("sebi update", "sebi", saved.getId());

        assertTrue(existsForDifferentId);
        assertFalse(existsForSameId);
    }

    @Test
    void fileAttachmentRepository_shouldPersistAndReadAttachmentMetadata() {
        FileAttachment attachment = new FileAttachment();
        attachment.setOriginalFilename("policy.pdf");
        attachment.setStoredFilename("uuid-policy.pdf");
        attachment.setContentType("application/pdf");
        attachment.setSizeBytes(1024L);
        attachment.setStoragePath("/tmp/uuid-policy.pdf");
        attachment.setCreatedAt(LocalDateTime.now().minusHours(1));
        attachment.setUpdatedAt(LocalDateTime.now());

        FileAttachment saved = fileAttachmentRepository.save(attachment);

        assertNotNull(saved.getId());
        assertTrue(fileAttachmentRepository.findById(saved.getId()).isPresent());
    }
}
