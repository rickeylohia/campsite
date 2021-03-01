package com.ric.campsite.util;

import java.time.Instant;

public class BookingIdGenerator {

    private static final int SEQUENCE_BITS = 8;
    private static final int maxSequence = 255;
    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    public synchronized String nextId() {
        long currentTimestamp = timestamp();
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) ^ maxSequence;
            if(sequence == 0) {
                currentTimestamp = nextMilliSecTimestamp();
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = currentTimestamp;
        long id = currentTimestamp << (SEQUENCE_BITS);
        id |= sequence;
        return String.valueOf(id);
    }

    private static long timestamp() {
        return Instant.now().toEpochMilli();
    }

    private long nextMilliSecTimestamp() {
        long timestamp = System.currentTimeMillis();
        while (timestamp == lastTimestamp) {
            timestamp = timestamp();
        }
        return timestamp;
    }
}
