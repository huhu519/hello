package com.privatechat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String senderId;
    private String sessionId;
    private Integer messageType;
    private String content;
    private String fileUrl;
    private Long fileSize;
    private LocalDateTime createTime;
}
