package com.privatechat.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.privatechat.common.Result;
import com.privatechat.entity.InviteCode;
import com.privatechat.mapper.InviteCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InviteCodeService {
    @Autowired
    private InviteCodeMapper inviteCodeMapper;

    public boolean validateInviteCode(String code) {
        LambdaQueryWrapper<InviteCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InviteCode::getCode, code).eq(InviteCode::getStatus, 1);
        InviteCode inviteCode = inviteCodeMapper.selectOne(wrapper);
        if (inviteCode == null) return false;
        if (inviteCode.getMaxUseCount() > 0 && inviteCode.getUseCount() >= inviteCode.getMaxUseCount()) {
            return false;
        }
        return true;
    }

    public void useInviteCode(String code) {
        LambdaQueryWrapper<InviteCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InviteCode::getCode, code);
        InviteCode inviteCode = inviteCodeMapper.selectOne(wrapper);
        if (inviteCode != null) {
            inviteCode.setUseCount(inviteCode.getUseCount() + 1);
            inviteCodeMapper.updateById(inviteCode);
        }
    }

    public Result<InviteCode> createInviteCode(String remark, Integer maxUseCount) {
        InviteCode inviteCode = new InviteCode();
        inviteCode.setCode(RandomUtil.randomString(12).toUpperCase());
        inviteCode.setStatus(1);
        inviteCode.setUseCount(0);
        inviteCode.setMaxUseCount(maxUseCount);
        inviteCode.setRemark(remark);
        inviteCode.setCreateTime(LocalDateTime.now());
        inviteCodeMapper.insert(inviteCode);
        return Result.success(inviteCode);
    }

    public Result<List<InviteCode>> listInviteCodes() {
        List<InviteCode> list = inviteCodeMapper.selectList(null);
        return Result.success(list);
    }
}
