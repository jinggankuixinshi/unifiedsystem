package com.unified.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class SequenceGenerator {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static volatile String lastDate = "";

    public static synchronized String generate(String prefix) {
        String today = LocalDateTime.now().format(DATE_FORMAT);
        if (!today.equals(lastDate)) {
            lastDate = today;
            counter.set(0);
        }
        int seq = counter.incrementAndGet();
        return String.format("%s%s%06d", prefix, today, seq);
    }
}
