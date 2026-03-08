package com.wms.service;

import com.wms.dto.ScanOperationDTO;
import com.wms.vo.ScanResolveVO;

public interface ScanService {
    ScanResolveVO resolve(String code);

    void operate(ScanOperationDTO dto);
}
