package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.Record;
import com.wms.mapper.RecordMapper;
import com.wms.service.RecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

    @Resource
    private RecordMapper recordMapper;

    @Override
    public IPage pageCC(IPage<Record> page, Wrapper wrapper) {
        return recordMapper.pageCC(page, wrapper);
    }
}
