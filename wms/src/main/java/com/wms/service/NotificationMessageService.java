package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.NotificationMessage;

public interface NotificationMessageService extends IService<NotificationMessage> {
    void createSystemNotification(String title, String content, String relatedType, Integer relatedId);

    void createEmailNotification(String title, String content, String recipient, String relatedType, Integer relatedId);
}
