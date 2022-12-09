package com.yujutg.upload.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yujutg.upload.entity.Users;

import java.util.List;
import java.util.concurrent.Future;

public interface UserService extends IService<Users> {

    public void truncateUser();

    Future<List<Users>> saveBatch(List<Users> users, String userName, int batchSize);
}
