package com.internship.tool.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.internship.tool.dto.RegulatoryChangeRequest;
import com.internship.tool.dto.RegulatoryChangeResponse;
import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.exception.DuplicateResourceException;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.RegulatoryChangeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RegulatoryChangeServiceImplTest {

    @Mock
    private RegulatoryChangeRepository regulatoryChangeRepository;

    @Mock
    private NotificationEmailService notificationEmailService;

    @InjectMocks
    private RegulatoryChangeServiceImpl regulatoryChangeService;

    private RegulatoryChangeRequest validRequest;
    private RegulatoryChange existingEntity;

    @BeforeEach
    void setUp() {
        validRequest = buildValidRequest();
        existingEntity = buildEntity(1L);
    }

    @Test
    void create_shouldReturnSavedResponse_whenRequestIsValid() {
        when(regulatoryChangeRepository.existsByTitleIgnoreCaseAndSourceIgnoreCase("RBI Circular", "RBI"))
                .thenReturn(false);
        when(regulatoryChangeRepository.save(any(RegulatoryChange.class))).thenReturn(existingEntity);

        RegulatoryChangeResponse response = regulatoryChangeService.create(validRequest);

        assertEquals(1L, response.getId());
        assertEquals("RBI Circular", response.getTitle());
        verify(regulatoryChangeRepository, times(1)).save(any(RegulatoryChange.class));
        verify(notificationEmailService, times(1)).sendCreateNotification(existingEntity);
    }

    @Test
    void create_shouldThrowDuplicateResourceException_whenTitleAndSourceAlreadyExist() {
        when(regulatoryChangeRepository.existsByTitleIgnoreCaseAndSourceIgnoreCase("RBI Circular", "RBI"))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> regulatoryChangeService.create(validRequest)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(regulatoryChangeRepository, never()).save(any(RegulatoryChange.class));
        verify(notificationEmailService, never()).sendCreateNotification(any(RegulatoryChange.class));
    }

    @Test
    void update_shouldReturnUpdatedResponse_whenIdAndRequestAreValid() {
        when(regulatoryChangeRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(regulatoryChangeRepository.existsByTitleIgnoreCaseAndSourceIgnoreCaseAndIdNot("RBI Circular", "RBI", 1L))
                .thenReturn(false);
        when(regulatoryChangeRepository.save(any(RegulatoryChange.class))).thenReturn(existingEntity);

        RegulatoryChangeResponse response = regulatoryChangeService.update(1L, validRequest);

        assertEquals(1L, response.getId());
        assertEquals("RBI Circular", response.getTitle());
        verify(regulatoryChangeRepository, times(1)).save(existingEntity);
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenIdDoesNotExist() {
        when(regulatoryChangeRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> regulatoryChangeService.update(99L, validRequest)
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(regulatoryChangeRepository, never()).save(any(RegulatoryChange.class));
    }

    @Test
    void getById_shouldReturnResponse_whenEntityExists() {
        when(regulatoryChangeRepository.findById(1L)).thenReturn(Optional.of(existingEntity));

        RegulatoryChangeResponse response = regulatoryChangeService.getById(1L);

        assertEquals(1L, response.getId());
        assertEquals("RBI Circular", response.getTitle());
        verify(regulatoryChangeRepository, times(1)).findById(1L);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenEntityDoesNotExist() {
        when(regulatoryChangeRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> regulatoryChangeService.getById(2L)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void getAll_shouldReturnPagedResponses_whenDataExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegulatoryChange> entityPage = new PageImpl<>(List.of(existingEntity), pageable, 1);
        when(regulatoryChangeRepository.findAll(pageable)).thenReturn(entityPage);

        Page<RegulatoryChangeResponse> responsePage = regulatoryChangeService.getAll(pageable);

        assertEquals(1, responsePage.getTotalElements());
        assertEquals("RBI Circular", responsePage.getContent().get(0).getTitle());
        verify(regulatoryChangeRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAll_shouldPropagateRuntimeException_whenRepositoryFails() {
        Pageable pageable = PageRequest.of(0, 10);
        when(regulatoryChangeRepository.findAll(pageable)).thenThrow(new RuntimeException("database down"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> regulatoryChangeService.getAll(pageable));

        assertEquals("database down", exception.getMessage());
    }

    @Test
    void delete_shouldDeleteEntity_whenIdExists() {
        when(regulatoryChangeRepository.existsById(1L)).thenReturn(true);

        regulatoryChangeService.delete(1L);

        verify(regulatoryChangeRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenIdDoesNotExist() {
        when(regulatoryChangeRepository.existsById(5L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> regulatoryChangeService.delete(5L)
        );

        assertTrue(exception.getMessage().contains("not found"));
        verify(regulatoryChangeRepository, never()).deleteById(any(Long.class));
    }

    private RegulatoryChangeRequest buildValidRequest() {
        RegulatoryChangeRequest request = new RegulatoryChangeRequest();
        request.setTitle("RBI Circular");
        request.setDescription("Updated KYC norms for all regulated entities");
        request.setSource("RBI");
        request.setJurisdiction("India");
        request.setCategory("Compliance");
        request.setStatus("NEW");
        request.setPriority("HIGH");
        request.setPublishedDate(LocalDate.now().minusDays(2));
        request.setEffectiveDate(LocalDate.now().plusDays(5));
        return request;
    }

    private RegulatoryChange buildEntity(Long id) {
        RegulatoryChange entity = new RegulatoryChange();
        entity.setId(id);
        entity.setTitle("RBI Circular");
        entity.setDescription("Updated KYC norms for all regulated entities");
        entity.setSource("RBI");
        entity.setJurisdiction("India");
        entity.setCategory("Compliance");
        entity.setStatus("NEW");
        entity.setPriority("HIGH");
        entity.setPublishedDate(LocalDate.now().minusDays(2));
        entity.setEffectiveDate(LocalDate.now().plusDays(5));
        entity.setCreatedAt(LocalDateTime.now().minusHours(1));
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
