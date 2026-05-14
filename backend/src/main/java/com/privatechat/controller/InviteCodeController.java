package com.privatechat.controller;

import com.privatechat.common.Result;
import com.privatechat.entity.InviteCode;
import com.privatechat.service.InviteCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/invite")
public class InviteCodeController {
    @Autowired
    private InviteCodeService inviteCodeService;

    @PostMapping("/create")
    public Result<InviteCode> create(@RequestParam String remark, @RequestParam Integer maxUseCount) {
        return inviteCodeService.createInviteCode(remark, maxUseCount);
    }

    @GetMapping("/list")
    public Result<List<InviteCode>> list() {
        return inviteCodeService.listInviteCodes();
    }
}
