package com.unified.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.system.entity.SysMessage;

public interface SysMessageService extends IService<SysMessage> {

    IPage<SysMessage> pageMessages(Long userId, int pageNum, int pageSize);

    long getUnreadCount(Long userId);

    void markRead(Long messageId, Long userId);

    void markAllRead(Long userId);

    void sendMessage(Long senderId, Long receiverId, String title, String content, String businessType, Long businessId);
}
