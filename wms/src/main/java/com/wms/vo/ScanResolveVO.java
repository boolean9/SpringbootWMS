package com.wms.vo;

import com.wms.entity.Goods;
import com.wms.entity.GoodsBatch;
import lombok.Data;

import java.util.List;

@Data
public class ScanResolveVO {
    private String code;
    private Goods goods;
    private List<GoodsBatch> batches;
    private boolean matched;
}
