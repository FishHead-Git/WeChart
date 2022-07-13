package com.yujutg.controller;


import com.yujutg.entity.User;
import com.yujutg.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Fishhead
 * @since 2020-09-23
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有User用户
     * @return List集合
     */
    @GetMapping("/")
    public List getAll(){
        return userService.list(null);
    }

    /**
     * 插入一个User用户
     * @param user 前端传入的用户名和密码
     * @return 返回是否插入成功
     */
    @PostMapping("/")
    public Boolean saveOne(User user){
        if(user != null){
            return userService.save(user);
        }
        return false;
    }

}

