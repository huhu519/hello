package com.privatechat.controller;

import com.privatechat.common.Result;
import com.privatechat.entity.User;
import com.privatechat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestParam String phone, @RequestParam String password,
                                   @RequestParam String nickname, @RequestParam String avatar,
                                   @RequestParam String inviteCode) {
        return userService.register(phone, password, nickname, avatar, inviteCode);
    }

    @PostMapping("/login")
    public Result<User> login(@RequestParam String account, @RequestParam String password) {
        return userService.login(account, password);
    }

    @GetMapping("/info/{userId}")
    public Result<User> getUserInfo(@PathVariable String userId) {
        return userService.getUserInfo(userId);
    }
}
