package com.privatechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.privatechat.common.Result;
import com.privatechat.entity.*;
import com.privatechat.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ChatService {
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${file.upload-path}")
    private String uploadPath;
    @Value("${file.max-total-size}")
    private Long maxTotalSize;
    @Value("${file.max-session-size}")
    private Long maxSessionSize;

    private static final Pattern EMOJI_PATTERN = Pattern.compile("[\uD83C-\uDBFF\uDC00-\uDFFF]|[\u200D\u2600-\u2BFF]");

    public Result<ChatMessage> sendMessage(String senderId, String sessionId, Integer messageType, String content, MultipartFile file) {
        if (messageType == 0 && EMOJI_PATTERN.matcher(content).find()) {
            return Result.error("禁止发送表情");
        }

        ChatSession session = getOrCreateSession(sessionId);
        if (session.getStorageUsed() >= maxSessionSize) {
            return Result.error("会话容量已满");
        }

        Long totalUsed = getTotalStorageUsed();
        if (totalUsed >= maxTotalSize) {
            return Result.error("总容量已满");
        }

        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setSessionId(sessionId);
        message.setMessageType(messageType);
        message.setCreateTime(LocalDateTime.now());

        if (messageType == 0) {
            message.setContent(content);
            message.setFileSize(0L);
        } else {
            try {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                File destFile = new File(uploadPath + "/" + fileName);
                destFile.getParentFile().mkdirs();
                file.transferTo(destFile);
                message.setFileUrl("/files/" + fileName);
                message.setFileSize(file.getSize());
            } catch (Exception e) {
                return Result.error("文件上传失败");
            }
        }

        chatMessageMapper.insert(message);
        updateSessionStorage(sessionId, message.getFileSize());
        updateUserStorage(senderId, message.getFileSize());

        messagingTemplate.convertAndSend("/topic/chat/" + sessionId, message);
        return Result.success(message);
    }

    public Result<List<ChatMessage>> getMessages(String sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId).orderByAsc(ChatMessage::getCreateTime);
        return Result.success(chatMessageMapper.selectList(wrapper));
    }

    private ChatSession getOrCreateSession(String sessionId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getSessionId, sessionId);
        ChatSession session = chatSessionMapper.selectOne(wrapper);
        if (session == null) {
            session = new ChatSession();
            session.setSessionId(sessionId);
            session.setSessionType(sessionId.startsWith("group_") ? 1 : 0);
            session.setStorageUsed(0L);
            session.setCreateTime(LocalDateTime.now());
            chatSessionMapper.insert(session);
        }
        return session;
    }

    private void updateSessionStorage(String sessionId, Long size) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getSessionId, sessionId);
        ChatSession session = chatSessionMapper.selectOne(wrapper);
        session.setStorageUsed(session.getStorageUsed() + size);
        chatSessionMapper.updateById(session);
    }

    private void updateUserStorage(String userId, Long size) {
        User user = userMapper.selectById(userId);
        user.setStorageUsed(user.getStorageUsed() + size);
        userMapper.updateById(user);
    }

    private Long getTotalStorageUsed() {
        List<ChatSession> sessions = chatSessionMapper.selectList(null);
        return sessions.stream().mapToLong(ChatSession::getStorageUsed).sum();
    }
}
