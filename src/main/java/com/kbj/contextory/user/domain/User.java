package com.kbj.contextory.user.domain;

import com.kbj.contextory.Entity.BaseEntity;
import com.kbj.contextory.login.domain.RefreshToken;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String providerId;

    @Column(unique = true)
    private String loginId;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshToken refreshToken;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String s3ImageKey;

    @Column
    private String password;

    @Column
    private String salt;

    @Builder(access = AccessLevel.PUBLIC)
    private User(String username, String loginId, String providerId,
                 String introduction, String password, String salt) {
        this.username = username;
        this.loginId = loginId;
        this.providerId = providerId;
        this.introduction = introduction;
        this.password = password;
        this.salt = salt;
        this.role = UserRole.USER;
    }

    public void updateProfile(String username, String introduction) {
        if (username != null) this.username = username;
        if (introduction != null) this.introduction = introduction;
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }
}
