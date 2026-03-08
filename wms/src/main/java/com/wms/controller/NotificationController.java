package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.NotificationMessage;
import com.wms.service.NotificationMessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private NotificationMessageService notificationMessageService;

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String keyword = (String) param.get("keyword");
        String channel = (String) param.get("channel");
        String status = (String) param.get("status");

        Page<NotificationMessage> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<NotificationMessage> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword) && !"null".equals(keyword)) {
            wrapper.and(item -> item.like(NotificationMessage::getTitle, keyword)
                    .or().like(NotificationMessage::getContent, keyword)
                    .or().like(NotificationMessage::getRecipient, keyword));
        }
        if (StringUtils.isNotBlank(channel) && !"null".equals(channel)) {
            wrapper.eq(NotificationMessage::getChannel, channel);
        }
        if (StringUtils.isNotBlank(status) && !"null".equals(status)) {
            wrapper.eq(NotificationMessage::getStatus, status);
        }
        wrapper.orderByDesc(NotificationMessage::getCreatedAt);

        IPage<NotificationMessage> result = notificationMessageService.page(page, wrapper);
        return Result.suc(result.getRecords(), result.getTotal());
    }

    @GetMapping("/recent")
    public Result recent() {
        return Result.suc(notificationMessageService.lambdaQuery()
                .orderByDesc(NotificationMessage::getCreatedAt)
                .last("limit 8")
                .list());
    }

    @GetMapping("/markRead")
    public Result markRead(@RequestParam Integer id) {
        NotificationMessage message = notificationMessageService.getById(id);
        if (message == null) {
            return Result.fail("消息不存在");
        }
        message.setStatus("READ");
        return notificationMessageService.updateById(message) ? Result.suc() : Result.fail("更新消息失败");
    }
}
