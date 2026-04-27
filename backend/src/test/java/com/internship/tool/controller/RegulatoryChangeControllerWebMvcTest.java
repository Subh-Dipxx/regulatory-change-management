package com.internship.tool.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.config.JwtAuthFilter;
import com.internship.tool.dto.RegulatoryChangeRequest;
import com.internship.tool.dto.RegulatoryChangeResponse;
import com.internship.tool.exception.GlobalExceptionHandler;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.service.RegulatoryChangeService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RegulatoryChangeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RegulatoryChangeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegulatoryChangeService regulatoryChangeService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getById_shouldReturn200_whenRecordExists() throws Exception {
        RegulatoryChangeResponse response = new RegulatoryChangeResponse();
        response.setId(1L);
        response.setTitle("RBI Circular");

        when(regulatoryChangeService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/regulatory-changes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("RBI Circular"));
    }

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        RegulatoryChangeRequest request = new RegulatoryChangeRequest();
        request.setTitle("RBI Circular");
        request.setDescription("Updated KYC");
        request.setSource("RBI");
        request.setJurisdiction("India");
        request.setCategory("Compliance");
        request.setStatus("NEW");
        request.setPriority("HIGH");
        request.setPublishedDate(LocalDate.now().minusDays(1));
        request.setEffectiveDate(LocalDate.now().plusDays(1));

        RegulatoryChangeResponse response = new RegulatoryChangeResponse();
        response.setId(10L);
        response.setTitle("RBI Circular");
        response.setCreatedAt(LocalDateTime.now());

        when(regulatoryChangeService.create(any(RegulatoryChangeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/regulatory-changes/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getById_shouldReturn404_whenRecordMissing() throws Exception {
        when(regulatoryChangeService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Regulatory change not found for id: 99"));

        mockMvc.perform(get("/api/v1/regulatory-changes/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("not found")))
                .andExpect(jsonPath("$.status").value(404));
    }
}
