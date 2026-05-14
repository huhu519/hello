package com.privatechat.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.privatechat.common.Result;
import com.privatechat.entity.User;
import com.privatechat.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import cn.hutool.crypto.digest.DigestUtil;
import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private InviteCodeService inviteCodeService;

    @Value("${file.max-users:15}")
    private Integer maxUsers;

    public Result<User> register(String phone, String password, String nickname, String avatar, String inviteCode) {
        if (!inviteCodeService.validateInviteCode(inviteCode)) {
            return Result.error("无效的邀请码");
        }
        
        LambdaQueryWrapper<User> countWrapper = new LambdaQueryWrapper<>();
        long userCount = userMapper.selectCount(countWrapper);
        if (userCount >= maxUsers) {
            return Result.error("已达到最大用户数限制");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        if (userMapper.selectOne(wrapper) != null) {
            return Result.error("手机号已注册");
        }

        String userId;
        do {
            userId = RandomUtil.randomNumbers(8);
        } while (userId.startsWith("0") || userMapper.selectById(userId) != null);

        User user = new User();
        user.setUserId(userId);
        user.setPhone(phone);
        user.setPassword(DigestUtil.md5Hex(password));
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setCreateTime(LocalDateTime.now());
        user.setDisabled(0);
        user.setStorageUsed(0L);
        userMapper.insert(user);

        inviteCodeService.useInviteCode(inviteCode);
        return Result.success(user);
    }

    public Result<User> login(String account, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getPhone, account).or().eq(User::getUserId, account));
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(DigestUtil.md5Hex(password))) {
            return Result.error("密码错误");
        }
        if (user.getDisabled() == 1) {
            return Result.error("账号已禁用");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    public Result<User> getUserInfo(String userId) {
        User user = userMapper.selectById(userId);
        user.setPassword(null);
        return Result.success(user);
    }
}
