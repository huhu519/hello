package com.privatechat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("invite_code")
public class InviteCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private Integer status;
    private Integer useCount;
    private Integer maxUseCount;
    private String remark;
    private LocalDateTime createTime;
}
