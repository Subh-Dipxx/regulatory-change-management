package com.internship.tool.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Response payload for a regulatory change")
public class RegulatoryChangeResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "RBI Circular on KYC updates")
    private String title;
    @Schema(example = "Detailed description of the regulatory update and required actions")
    private String description;
    @Schema(example = "RBI")
    private String source;
    @Schema(example = "India")
    private String jurisdiction;
    @Schema(example = "Compliance")
    private String category;
    @Schema(example = "NEW")
    private String status;
    @Schema(example = "HIGH")
    private String priority;
    @Schema(example = "87", description = "Demo score from 0 to 100")
    private Integer score;
    @Schema(example = "2026-05-01", format = "date")
    private LocalDate effectiveDate;
    @Schema(example = "2026-04-25", format = "date")
    private LocalDate publishedDate;
    @Schema(example = "2026-04-27T10:15:30")
    private LocalDateTime createdAt;
    @Schema(example = "2026-04-27T10:45:30")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
