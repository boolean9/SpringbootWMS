package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.WarehouseTransfer;
import com.wms.mapper.WarehouseTransferMapper;
import com.wms.service.WarehouseTransferService;
import org.springframework.stereotype.Service;

@Service
public class WarehouseTransferServiceImpl extends ServiceImpl<WarehouseTransferMapper, WarehouseTransfer> implements WarehouseTransferService {
}
