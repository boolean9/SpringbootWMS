package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.Goodstype;
import com.wms.mapper.GoodstypeMapper;
import com.wms.service.GoodstypeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class GoodstypeServiceImpl extends ServiceImpl<GoodstypeMapper, Goodstype> implements GoodstypeService {

    @Resource
    private GoodstypeMapper goodstypeMapper;

    @Override
    public IPage pageCC(IPage<Goodstype> page, Wrapper wrapper) {
        return goodstypeMapper.pageCC(page, wrapper);
    }
}
