package com.privatechat.controller;

import com.privatechat.common.Result;
import com.privatechat.entity.ChatMessage;
import com.privatechat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    public Result<ChatMessage> sendMessage(@RequestParam String senderId, @RequestParam String sessionId,
                                            @RequestParam Integer messageType, @RequestParam(required = false) String content,
                                            @RequestParam(required = false) MultipartFile file) {
        return chatService.sendMessage(senderId, sessionId, messageType, content, file);
    }

    @GetMapping("/messages/{sessionId}")
    public Result<List<ChatMessage>> getMessages(@PathVariable String sessionId) {
        return chatService.getMessages(sessionId);
    }
}
