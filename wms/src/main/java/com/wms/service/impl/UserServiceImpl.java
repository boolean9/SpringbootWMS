package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.User;
import com.wms.mapper.UserMapper;
import com.wms.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public IPage pageC(IPage<User> page) {
        return userMapper.pageC(page);
    }

    @Override
    public IPage pageCC(IPage<User> page, Wrapper wrapper) {
        return userMapper.pageCC(page, wrapper);
    }
}
