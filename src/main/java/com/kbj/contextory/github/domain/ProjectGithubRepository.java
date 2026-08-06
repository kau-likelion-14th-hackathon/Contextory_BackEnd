package com.kbj.contextory.github.domain;

import com.kbj.contextory.Entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@Table(name = "project_repositories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectGithubRepository extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repository_id")
    private Long repositoryId;

    @Column(name = "project_id", nullable = false, unique = true)
    private Long projectId;

    @Column(name = "connected_by", nullable = false)
    private Long connectedBy;

    @Column(name = "github_repository_id", nullable = false)
    private Long githubRepositoryId;

    @Column(name = "repository_full_name", nullable = false, length = 511)
    private String repositoryFullName;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Builder
    private ProjectGithubRepository(
            Long projectId,
            Long connectedBy,
            Long githubRepositoryId,
            String repositoryFullName,
            Instant lastSyncedAt
    ) {
        this.projectId = projectId;
        this.connectedBy = connectedBy;
        this.githubRepositoryId = githubRepositoryId;
        this.repositoryFullName = repositoryFullName;
        this.lastSyncedAt = lastSyncedAt;
    }

    public void reconnect(
            Long connectedBy,
            Long githubRepositoryId,
            String repositoryFullName,
            Instant syncedAt
    ) {
        this.connectedBy = connectedBy;
        this.githubRepositoryId = githubRepositoryId;
        this.repositoryFullName = repositoryFullName;
        this.lastSyncedAt = syncedAt;
    }

    public void synchronize(
            Long githubRepositoryId,
            String repositoryFullName,
            Instant syncedAt
    ) {
        this.githubRepositoryId = githubRepositoryId;
        this.repositoryFullName = repositoryFullName;
        this.lastSyncedAt = syncedAt;
    }
}
