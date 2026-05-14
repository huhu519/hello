package com.privatechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.privatechat.common.Result;
import com.privatechat.entity.Friend;
import com.privatechat.entity.User;
import com.privatechat.mapper.FriendMapper;
import com.privatechat.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FriendService {
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private UserMapper userMapper;

    public Result<String> sendFriendRequest(String userId, String friendId) {
        if (userId.equals(friendId)) {
            return Result.error("不能添加自己为好友");
        }
        LambdaQueryWrapper<Friend> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        if (friendMapper.selectOne(checkWrapper) != null) {
            return Result.error("已经是好友或申请中");
        }

        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(0);
        friend.setCreateTime(LocalDateTime.now());
        friendMapper.insert(friend);
        return Result.success("申请已发送");
    }

    public Result<String> handleFriendRequest(Long id, Integer accept) {
        Friend friend = friendMapper.selectById(id);
        if (friend == null) {
            return Result.error("申请不存在");
        }
        if (accept == 1) {
            friend.setStatus(1);
            friendMapper.updateById(friend);
            
            Friend reverse = new Friend();
            reverse.setUserId(friend.getFriendId());
            reverse.setFriendId(friend.getUserId());
            reverse.setStatus(1);
            reverse.setCreateTime(LocalDateTime.now());
            friendMapper.insert(reverse);
        } else {
            friendMapper.deleteById(id);
        }
        return Result.success("操作成功");
    }

    public Result<List<User>> listFriends(String userId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId).eq(Friend::getStatus, 1);
        List<Friend> friends = friendMapper.selectList(wrapper);
        List<User> users = new ArrayList<>();
        for (Friend f : friends) {
            User user = userMapper.selectById(f.getFriendId());
            user.setPassword(null);
            users.add(user);
        }
        return Result.success(users);
    }
}
