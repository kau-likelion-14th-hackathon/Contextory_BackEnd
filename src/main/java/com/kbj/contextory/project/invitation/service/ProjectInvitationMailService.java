package com.kbj.contextory.project.invitation.service;

import com.kbj.contextory.global.api.ErrorCode;
import com.kbj.contextory.global.exception.GeneralException;
import com.kbj.contextory.project.domain.Project;
import com.kbj.contextory.project.invitation.domain.ProjectInvitation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ProjectInvitationMailService {

    private final JavaMailSender mailSender;

    @Value("${contextory.invitation.accept-url}")
    private String invitationAcceptUrl;

    @Value("${contextory.invitation.mail-from}")
    private String mailFrom;

    public void sendInvitation(Project project, ProjectInvitation invitation) {
        String acceptUrl = UriComponentsBuilder
                .fromUriString(invitationAcceptUrl)
                .queryParam("token", invitation.getInviteToken())
                .build()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(invitation.getInviteEmail());
        message.setSubject(
                "[Contextory] " + project.getName() + " 프로젝트 초대"
        );
        message.setText(
                "Contextory 프로젝트 초대가 도착했습니다.\n\n"
                        + "프로젝트: " + project.getName() + "\n"
                        + "권한: " + invitation.getPermissionRole().name() + "\n"
                        + "프로젝트 역할: "
                        + (invitation.getProjectRole() == null
                        ? "미지정"
                        : invitation.getProjectRole())
                        + "\n"
                        + "만료 시각: "
                        + DateTimeFormatter.ISO_INSTANT
                        .format(invitation.getExpiresAt())
                        + "\n\n"
                        + "초대 수락 링크:\n"
                        + acceptUrl
                        + "\n\n"
                        + "로그인 후 초대를 수락해주세요."
        );

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw GeneralException.of(ErrorCode.PROJECT_INVITATION_EMAIL_SEND_FAILED);
        }
    }
}
