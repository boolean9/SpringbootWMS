package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.Goods;
import com.wms.entity.GoodsBatch;
import com.wms.entity.InventoryAlert;
import com.wms.entity.Storage;
import com.wms.mapper.InventoryAlertMapper;
import com.wms.service.GoodsBatchService;
import com.wms.service.GoodsService;
import com.wms.service.InventoryAlertService;
import com.wms.service.NotificationMessageService;
import com.wms.service.StorageService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class InventoryAlertServiceImpl extends ServiceImpl<InventoryAlertMapper, InventoryAlert>
        implements InventoryAlertService {

    @Resource
    private GoodsService goodsService;

    @Resource
    private GoodsBatchService goodsBatchService;

    @Resource
    private StorageService storageService;

    @Resource
    private NotificationMessageService notificationMessageService;

    @Value("${wms.alert.mail.recipients:}")
    private String recipients;

    @Override
    public void refreshAlerts() {
        goodsService.list().forEach(this::evaluateGoodsAlert);
        goodsBatchService.list().forEach(this::evaluateBatchAlert);
    }

    @Override
    public void evaluateGoodsAlert(Goods goods) {
        if (goods == null) {
            return;
        }

        int currentCount = goods.getCount() == null ? 0 : goods.getCount();
        int minStock = goods.getMinStock() == null ? 0 : goods.getMinStock();
        int maxStock = goods.getMaxStock() == null ? 0 : goods.getMaxStock();
        Storage storage = goods.getStorage() == null ? null : storageService.getById(goods.getStorage());
        String storageName = storage == null ? "-" : storage.getName();

        if (minStock > 0 && currentCount < minStock) {
            upsertAlert(
                    "GOODS_LOW_" + goods.getId(),
                    goods.getId(),
                    goods.getName(),
                    goods.getStorage(),
                    storageName,
                    null,
                    null,
                    "LOW_STOCK",
                    "HIGH",
                    "货品库存低于安全阈值",
                    BigDecimal.valueOf(currentCount),
                    BigDecimal.valueOf(minStock)
            );
        } else {
            resolveAlert("GOODS_LOW_" + goods.getId());
        }

        if (maxStock > 0 && currentCount > maxStock) {
            upsertAlert(
                    "GOODS_HIGH_" + goods.getId(),
                    goods.getId(),
                    goods.getName(),
                    goods.getStorage(),
                    storageName,
                    null,
                    null,
                    "HIGH_STOCK",
                    "MEDIUM",
                    "货品库存超过上限阈值",
                    BigDecimal.valueOf(currentCount),
                    BigDecimal.valueOf(maxStock)
            );
        } else {
            resolveAlert("GOODS_HIGH_" + goods.getId());
        }
    }

    @Override
    public void evaluateBatchAlert(GoodsBatch batch) {
        if (batch == null || batch.getExpiryDate() == null) {
            if (batch != null) {
                resolveAlert("BATCH_EXPIRY_" + batch.getId());
            }
            return;
        }

        long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate());
        int alertDays = batch.getAlertDays() == null ? 30 : batch.getAlertDays();
        if (remainingDays <= alertDays) {
            Goods goods = goodsService.getById(batch.getGoodsId());
            Storage storage = batch.getStorageId() == null ? null : storageService.getById(batch.getStorageId());
            String goodsName = goods == null ? "-" : goods.getName();
            String storageName = storage == null ? "-" : storage.getName();
            String alertType = remainingDays < 0 ? "EXPIRED" : "EXPIRY";
            String alertLevel = remainingDays < 0 ? "HIGH" : "MEDIUM";
            String content = remainingDays < 0 ? "批次已过期，请尽快处理" : "批次即将过期，请提前安排处理";

            upsertAlert(
                    "BATCH_EXPIRY_" + batch.getId(),
                    batch.getGoodsId(),
                    goodsName,
                    batch.getStorageId(),
                    storageName,
                    batch.getId(),
                    batch.getBatchNo(),
                    alertType,
                    alertLevel,
                    content,
                    BigDecimal.valueOf(remainingDays),
                    BigDecimal.valueOf(alertDays)
            );
        } else {
            resolveAlert("BATCH_EXPIRY_" + batch.getId());
        }
    }

    private void upsertAlert(
            String sourceKey,
            Integer goodsId,
            String goodsName,
            Integer storageId,
            String storageName,
            Integer batchId,
            String batchNo,
            String alertType,
            String alertLevel,
            String content,
            BigDecimal currentValue,
            BigDecimal thresholdValue
    ) {
        InventoryAlert alert = lambdaQuery().eq(InventoryAlert::getSourceKey, sourceKey).one();
        boolean shouldNotify = false;

        if (alert == null) {
            alert = new InventoryAlert();
            alert.setSourceKey(sourceKey);
            alert.setCreatedAt(LocalDateTime.now());
            alert.setStatus("UNREAD");
            alert.setNotified(false);
            shouldNotify = true;
        } else if ("RESOLVED".equals(alert.getStatus())) {
            alert.setStatus("UNREAD");
            alert.setNotified(false);
            shouldNotify = true;
        }

        alert.setGoodsId(goodsId);
        alert.setGoodsName(goodsName);
        alert.setStorageId(storageId);
        alert.setStorageName(storageName);
        alert.setBatchId(batchId);
        alert.setBatchNo(batchNo);
        alert.setAlertType(alertType);
        alert.setAlertLevel(alertLevel);
        alert.setContent(content);
        alert.setCurrentValue(currentValue);
        alert.setThresholdValue(thresholdValue);
        alert.setNotifyChannels(recipients == null || recipients.trim().isEmpty() ? "SYSTEM" : "SYSTEM,EMAIL");
        alert.setUpdatedAt(LocalDateTime.now());

        if (alert.getId() == null) {
            save(alert);
        } else {
            updateById(alert);
        }

        if (shouldNotify || !Boolean.TRUE.equals(alert.getNotified())) {
            notifyAlert(alert);
        }
    }

    private void notifyAlert(InventoryAlert alert) {
        String title = "WMS 预警通知";
        String content = String.format(
                "%s | 货品: %s | 仓库: %s%s",
                alert.getContent(),
                alert.getGoodsName(),
                alert.getStorageName(),
                alert.getBatchNo() == null ? "" : " | 批次: " + alert.getBatchNo()
        );

        notificationMessageService.createSystemNotification(title, content, "ALERT", alert.getId());

        if (recipients != null && !recipients.trim().isEmpty()) {
            List<String> recipientList = Arrays.stream(recipients.split(","))
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .toList();
            for (String recipient : recipientList) {
                notificationMessageService.createEmailNotification(title, content, recipient, "ALERT", alert.getId());
            }
        }

        alert.setNotified(true);
        updateById(alert);
    }

    private void resolveAlert(String sourceKey) {
        InventoryAlert alert = lambdaQuery().eq(InventoryAlert::getSourceKey, sourceKey).one();
        if (alert == null || "RESOLVED".equals(alert.getStatus())) {
            return;
        }

        alert.setStatus("RESOLVED");
        alert.setUpdatedAt(LocalDateTime.now());
        updateById(alert);
    }
}
