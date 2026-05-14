package com.privatechat.controller;

import com.privatechat.common.Result;
import com.privatechat.entity.User;
import com.privatechat.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/friend")
public class FriendController {
    @Autowired
    private FriendService friendService;

    @PostMapping("/request")
    public Result<String> sendRequest(@RequestParam String userId, @RequestParam String friendId) {
        return friendService.sendFriendRequest(userId, friendId);
    }

    @PostMapping("/handle")
    public Result<String> handleRequest(@RequestParam Long id, @RequestParam Integer accept) {
        return friendService.handleFriendRequest(id, accept);
    }

    @GetMapping("/list/{userId}")
    public Result<List<User>> listFriends(@PathVariable String userId) {
        return friendService.listFriends(userId);
    }
}
