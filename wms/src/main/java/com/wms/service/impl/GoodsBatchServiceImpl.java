package com.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.GoodsBatch;
import com.wms.mapper.GoodsBatchMapper;
import com.wms.service.GoodsBatchService;
import org.springframework.stereotype.Service;

@Service
public class GoodsBatchServiceImpl extends ServiceImpl<GoodsBatchMapper, GoodsBatch> implements GoodsBatchService {
}
