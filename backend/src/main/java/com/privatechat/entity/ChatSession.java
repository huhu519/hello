package com.privatechat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_session")
public class ChatSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private Integer sessionType;
    private Long storageUsed;
    private LocalDateTime createTime;
}
