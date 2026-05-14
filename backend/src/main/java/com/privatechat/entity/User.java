package com.privatechat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.INPUT)
    private String userId;
    private String phone;
    private String password;
    private String nickname;
    private String avatar;
    private LocalDateTime createTime;
    private Integer disabled;
    private Long storageUsed;
}
