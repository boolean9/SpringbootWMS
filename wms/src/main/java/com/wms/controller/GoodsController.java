package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.service.GoodsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    @PostMapping("/save")
    public Result save(@RequestBody Goods goods) {
        normalize(goods);
        return goodsService.save(goods) ? Result.suc() : Result.fail("保存货品失败");
    }

    @PostMapping("/update")
    public Result update(@RequestBody Goods goods) {
        normalize(goods);
        return goodsService.updateById(goods) ? Result.suc() : Result.fail("更新货品失败");
    }

    @GetMapping("/del")
    public Result del(@RequestParam String id) {
        return goodsService.removeById(id) ? Result.suc() : Result.fail("删除货品失败");
    }

    @GetMapping("/list")
    public Result list() {
        return Result.suc(goodsService.lambdaQuery().orderByDesc(Goods::getId).list());
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String name = (String) param.get("name");
        String goodstype = (String) param.get("goodstype");
        String storage = (String) param.get("storage");
        String supplierId = (String) param.get("supplierId");
        String code = (String) param.get("code");

        Page<Goods> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name) && !"null".equals(name)) {
            wrapper.like(Goods::getName, name);
        }
        if (StringUtils.isNotBlank(goodstype) && !"null".equals(goodstype)) {
            wrapper.eq(Goods::getGoodstype, Integer.parseInt(goodstype));
        }
        if (StringUtils.isNotBlank(storage) && !"null".equals(storage)) {
            wrapper.eq(Goods::getStorage, Integer.parseInt(storage));
        }
        if (StringUtils.isNotBlank(supplierId) && !"null".equals(supplierId)) {
            wrapper.eq(Goods::getSupplierId, Integer.parseInt(supplierId));
        }
        if (StringUtils.isNotBlank(code) && !"null".equals(code)) {
            wrapper.and(item -> item.like(Goods::getBarcode, code).or().like(Goods::getRfidTag, code));
        }
        wrapper.orderByDesc(Goods::getId);

        IPage<Goods> result = goodsService.page(page, wrapper);
        return Result.suc(result.getRecords(), result.getTotal());
    }

    private void normalize(Goods goods) {
        if (goods.getCount() == null) {
            goods.setCount(0);
        }
        if (goods.getMinStock() == null) {
            goods.setMinStock(0);
        }
        if (goods.getMaxStock() == null) {
            goods.setMaxStock(0);
        }
    }
}
