package com.unified.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.message.WebSocketNotifier;
import com.unified.system.entity.SysMessage;
import com.unified.system.mapper.SysMessageMapper;
import com.unified.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    private final WebSocketNotifier webSocketNotifier;

    @Override
    public IPage<SysMessage> pageMessages(Long userId, int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, userId)
                .orderByDesc(SysMessage::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return count(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, userId)
                .eq(SysMessage::getIsRead, 0));
    }

    @Override
    public void markRead(Long messageId, Long userId) {
        update(new LambdaUpdateWrapper<SysMessage>()
                .eq(SysMessage::getId, messageId)
                .eq(SysMessage::getReceiverId, userId)
                .set(SysMessage::getIsRead, 1)
                .set(SysMessage::getReadTime, LocalDateTime.now()));
    }

    @Override
    public void markAllRead(Long userId) {
        update(new LambdaUpdateWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, userId)
                .eq(SysMessage::getIsRead, 0)
                .set(SysMessage::getIsRead, 1)
                .set(SysMessage::getReadTime, LocalDateTime.now()));
    }

    @Override
    public void sendMessage(Long senderId, Long receiverId, String title, String content, String businessType, Long businessId) {
        SysMessage message = new SysMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setBusinessType(businessType);
        message.setBusinessId(businessId);
        message.setIsRead(0);
        save(message);
        webSocketNotifier.sendToUser(receiverId, "new_message", Map.of("title", title, "content", content));
    }
}
