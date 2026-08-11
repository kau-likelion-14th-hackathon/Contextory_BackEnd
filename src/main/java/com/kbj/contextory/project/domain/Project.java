package com.kbj.contextory.project.domain;

import com.kbj.contextory.Entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "projects")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "default_language", nullable = false, length = 50)
    private String defaultLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectStatus status;

    @Builder
    private Project(
            Long ownerId,
            String name,
            String slug,
            String summary,
            String purpose,
            String defaultLanguage
    ) {
        this.ownerId = ownerId;
        this.name = name;
        this.slug = slug;
        this.summary = summary;
        this.purpose = purpose;
        this.defaultLanguage = defaultLanguage == null ? "ko" : defaultLanguage;
        this.status = ProjectStatus.ACTIVE;
    }

    public void update(
            String name,
            String summary,
            String purpose,
            String defaultLanguage,
            ProjectStatus status
    ) {
        if (name != null) {
            this.name = name;
        }
        if (summary != null) {
            this.summary = summary;
        }
        if (purpose != null) {
            this.purpose = purpose;
        }
        if (defaultLanguage != null) {
            this.defaultLanguage = defaultLanguage;
        }
        if (status != null) {
            this.status = status;
        }
    }

    public void delete() {
        this.status = ProjectStatus.DELETED;
    }
}
