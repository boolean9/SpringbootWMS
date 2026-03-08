package com.wms.service;

import com.wms.entity.Record;
import com.wms.entity.WarehouseTransfer;

public interface InventoryFlowService {
    void processRecord(Record record);

    void executeTransfer(WarehouseTransfer transfer);
}
