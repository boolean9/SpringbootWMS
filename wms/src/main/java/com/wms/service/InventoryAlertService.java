package com.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.Goods;
import com.wms.entity.GoodsBatch;
import com.wms.entity.InventoryAlert;

public interface InventoryAlertService extends IService<InventoryAlert> {
    void refreshAlerts();

    void evaluateGoodsAlert(Goods goods);

    void evaluateBatchAlert(GoodsBatch batch);
}
