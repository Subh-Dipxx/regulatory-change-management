package com.internship.tool.service;

import com.internship.tool.dto.RegulatoryChangeRequest;
import com.internship.tool.dto.RegulatoryChangeResponse;
import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.exception.BusinessRuleViolationException;
import com.internship.tool.exception.DuplicateResourceException;
import com.internship.tool.exception.InputValidationException;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.RegulatoryChangeRepository;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegulatoryChangeServiceImpl implements RegulatoryChangeService {

    private static final Set<String> ALLOWED_STATUS = Set.of(
            "NEW", "UNDER_REVIEW", "IN_PROGRESS", "IMPLEMENTED", "CLOSED"
    );
    private static final Set<String> ALLOWED_PRIORITY = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

    private final RegulatoryChangeRepository regulatoryChangeRepository;
    private final NotificationEmailService notificationEmailService;

    public RegulatoryChangeServiceImpl(
            RegulatoryChangeRepository regulatoryChangeRepository,
            NotificationEmailService notificationEmailService
    ) {
        this.regulatoryChangeRepository = regulatoryChangeRepository;
        this.notificationEmailService = notificationEmailService;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "regulatoryChangesById", allEntries = true),
            @CacheEvict(cacheNames = "regulatoryChangesAll", allEntries = true)
    })
    public RegulatoryChangeResponse create(RegulatoryChangeRequest request) {
        validateRequest(request);

        String normalizedTitle = normalize(request.getTitle());
        String normalizedSource = normalize(request.getSource());
        if (regulatoryChangeRepository.existsByTitleIgnoreCaseAndSourceIgnoreCase(normalizedTitle, normalizedSource)) {
            throw new DuplicateResourceException("Regulatory change already exists for this title and source");
        }

        RegulatoryChange regulatoryChange = new RegulatoryChange();
        mapRequestToEntity(request, regulatoryChange);
        RegulatoryChange saved = regulatoryChangeRepository.save(regulatoryChange);
        notificationEmailService.sendCreateNotification(saved);
        return mapToResponse(saved);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "regulatoryChangesById", allEntries = true),
            @CacheEvict(cacheNames = "regulatoryChangesAll", allEntries = true)
    })
    public RegulatoryChangeResponse update(Long id, RegulatoryChangeRequest request) {
        validateId(id);
        validateRequest(request);

        RegulatoryChange existing = regulatoryChangeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regulatory change not found for id: " + id));

        String normalizedTitle = normalize(request.getTitle());
        String normalizedSource = normalize(request.getSource());
        if (regulatoryChangeRepository.existsByTitleIgnoreCaseAndSourceIgnoreCaseAndIdNot(normalizedTitle, normalizedSource, id)) {
            throw new DuplicateResourceException("Another regulatory change with same title and source already exists");
        }

        mapRequestToEntity(request, existing);
        RegulatoryChange updated = regulatoryChangeRepository.save(existing);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "regulatoryChangesById", key = "#id")
    public RegulatoryChangeResponse getById(Long id) {
        validateId(id);
        RegulatoryChange regulatoryChange = regulatoryChangeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regulatory change not found for id: " + id));
        return mapToResponse(regulatoryChange);
    }

    @Override
    @Transactional(readOnly = true)
        @Cacheable(
            cacheNames = "regulatoryChangesAll",
            key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
        )
    public Page<RegulatoryChangeResponse> getAll(Pageable pageable) {
        return regulatoryChangeRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
        @Caching(evict = {
            @CacheEvict(cacheNames = "regulatoryChangesById", allEntries = true),
            @CacheEvict(cacheNames = "regulatoryChangesAll", allEntries = true)
        })
    public void delete(Long id) {
        validateId(id);
        if (!regulatoryChangeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Regulatory change not found for id: " + id);
        }
        regulatoryChangeRepository.deleteById(id);
    }

    private void validateRequest(RegulatoryChangeRequest request) {
        if (request == null) {
            throw new InputValidationException("Request body cannot be null");
        }

        validateRequiredText("title", request.getTitle(), 255);
        validateRequiredText("description", request.getDescription(), 5000);
        validateRequiredText("source", request.getSource(), 255);
        validateRequiredText("jurisdiction", request.getJurisdiction(), 120);
        validateRequiredText("category", request.getCategory(), 120);
        validateRequiredText("status", request.getStatus(), 40);
        validateRequiredText("priority", request.getPriority(), 40);

        String normalizedStatus = normalize(request.getStatus()).toUpperCase();
        if (!ALLOWED_STATUS.contains(normalizedStatus)) {
            throw new InputValidationException("Invalid status. Allowed values: " + ALLOWED_STATUS);
        }

        String normalizedPriority = normalize(request.getPriority()).toUpperCase();
        if (!ALLOWED_PRIORITY.contains(normalizedPriority)) {
            throw new InputValidationException("Invalid priority. Allowed values: " + ALLOWED_PRIORITY);
        }

        LocalDate effectiveDate = request.getEffectiveDate();
        LocalDate publishedDate = request.getPublishedDate();
        if (effectiveDate != null && publishedDate != null && effectiveDate.isBefore(publishedDate)) {
            throw new BusinessRuleViolationException("effectiveDate cannot be before publishedDate");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new InputValidationException("Invalid id. It must be a positive number");
        }
    }

    private void validateRequiredText(String fieldName, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InputValidationException(fieldName + " is required");
        }
        String normalized = normalize(value);
        if (normalized.length() > maxLength) {
            throw new InputValidationException(fieldName + " exceeds max length of " + maxLength);
        }
    }

    private void mapRequestToEntity(RegulatoryChangeRequest request, RegulatoryChange entity) {
        entity.setTitle(normalize(request.getTitle()));
        entity.setDescription(normalize(request.getDescription()));
        entity.setSource(normalize(request.getSource()));
        entity.setJurisdiction(normalize(request.getJurisdiction()));
        entity.setCategory(normalize(request.getCategory()));
        entity.setStatus(normalize(request.getStatus()).toUpperCase());
        entity.setPriority(normalize(request.getPriority()).toUpperCase());
        entity.setEffectiveDate(request.getEffectiveDate());
        entity.setPublishedDate(request.getPublishedDate());
    }

    private RegulatoryChangeResponse mapToResponse(RegulatoryChange entity) {
        RegulatoryChangeResponse response = new RegulatoryChangeResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setSource(entity.getSource());
        response.setJurisdiction(entity.getJurisdiction());
        response.setCategory(entity.getCategory());
        response.setStatus(entity.getStatus());
        response.setPriority(entity.getPriority());
        response.setEffectiveDate(entity.getEffectiveDate());
        response.setPublishedDate(entity.getPublishedDate());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
