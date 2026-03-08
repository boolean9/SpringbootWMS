package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.NotificationMessage;
import com.wms.mapper.NotificationMessageMapper;
import com.wms.service.NotificationMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationMessageServiceImpl extends ServiceImpl<NotificationMessageMapper, NotificationMessage>
        implements NotificationMessageService {

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${wms.alert.mail.enabled:false}")
    private boolean mailEnabled;

    @Override
    public void createSystemNotification(String title, String content, String relatedType, Integer relatedId) {
        NotificationMessage message = new NotificationMessage();
        message.setTitle(title);
        message.setContent(content);
        message.setChannel("SYSTEM");
        message.setStatus("UNREAD");
        message.setRelatedType(relatedType);
        message.setRelatedId(relatedId);
        message.setCreatedAt(LocalDateTime.now());
        save(message);
    }

    @Override
    public void createEmailNotification(String title, String content, String recipient, String relatedType, Integer relatedId) {
        NotificationMessage message = new NotificationMessage();
        message.setTitle(title);
        message.setContent(content);
        message.setChannel("EMAIL");
        message.setRecipient(recipient);
        message.setStatus("PENDING");
        message.setRelatedType(relatedType);
        message.setRelatedId(relatedId);
        message.setCreatedAt(LocalDateTime.now());
        save(message);

        if (!mailEnabled || javaMailSender == null || recipient == null || recipient.trim().isEmpty()) {
            message.setStatus("SKIPPED");
            updateById(message);
            return;
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(recipient);
            mailMessage.setSubject(title);
            mailMessage.setText(content);
            javaMailSender.send(mailMessage);
            message.setStatus("SENT");
        } catch (Exception ex) {
            message.setStatus("FAILED");
        }

        updateById(message);
    }
}
