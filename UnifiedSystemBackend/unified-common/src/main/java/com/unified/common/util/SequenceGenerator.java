package com.unified.common.util;

import com.unified.common.sequence.SysSequenceService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 业务单号生成器（静态门面，保持原 API；底层走 sys_sequence 表序列）
 * 格式：前缀 + yyyyMMdd + 6 位序号
 */
@Component
public class SequenceGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static SysSequenceService sequenceService;

    public SequenceGenerator(SysSequenceService service) {
        SequenceGenerator.sequenceService = service;
    }

    public static String generate(String prefix) {
        if (sequenceService == null) {
            throw new IllegalStateException("SequenceGenerator 未初始化");
        }
        LocalDate today = LocalDate.now();
        int seq = sequenceService.nextSeq(prefix, today);
        return String.format("%s%s%06d", prefix, today.format(DATE_FORMAT), seq);
    }
}
