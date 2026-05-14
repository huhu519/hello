package com.privatechat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("group_chat")
public class GroupChat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String groupId;
    private String groupName;
    private String creatorId;
    private LocalDateTime createTime;
}
