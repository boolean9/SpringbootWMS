package com.wms.task;

import com.wms.service.InventoryAlertService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InventoryAlertScheduler {

    @Resource
    private InventoryAlertService inventoryAlertService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshAlerts() {
        inventoryAlertService.refreshAlerts();
    }
}
