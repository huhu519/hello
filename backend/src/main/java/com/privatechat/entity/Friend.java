package com.privatechat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend")
public class Friend {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String friendId;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
}
