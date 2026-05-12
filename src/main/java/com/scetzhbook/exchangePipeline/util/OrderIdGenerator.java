package com.scetzhbook.exchangePipeline.util;

import java.util.concurrent.atomic.AtomicLong;

import com.scetzhbook.exchangePipeline.model.Side;


public final class OrderIdGenerator {
    private static final int SEQ_BITS = 20;
    private static final long SEQ_MASK = (1L << SEQ_BITS) - 1L;

    private final AtomicLong last = new AtomicLong(0L);

    public String next(String symbol, Side side, double price, long quantity, long timestampMillis) {
        long packed = nextPacked(timestampMillis);
        int hashedAttributes = attrHash(symbol, side, price, quantity);
        return Long.toUnsignedString(packed, 36) + "-" + Integer.toUnsignedString(hashedAttributes & 0xFFFF, 36);
    }

    private long nextPacked(long nowMillis) {
        while (true) {
            long prev = last.get();
            long prevMillis = prev >>> SEQ_BITS;
            long prevSeq = prev & SEQ_MASK;

            long seq = (nowMillis == prevMillis) ? (prevSeq + 1) & SEQ_MASK : 0L;
            long next = (nowMillis << SEQ_BITS) | seq;

            if (last.compareAndSet(prev, next)) {
                return next;
            }
        }
    }

    private static int attrHash(String symbol, Side side, double price, long quantity) {
        long x = 0x9E3779B97F4A7C15L;
        if (symbol != null) {
            for (int i = 0, n = symbol.length(); i < n; i++) {
                x ^= symbol.charAt(i);
                x *= 0xBF58476D1CE4E5B9L;
                x ^= (x >>> 27);
            }
        }
        x ^= (side == null ? 0 : side.ordinal() + 1);
        x *= 0x94D049BB133111EBL;
        x ^= Double.doubleToLongBits(price);
        x *= 0xBF58476D1CE4E5B9L;
        x ^= quantity;
        x ^= (x >>> 33);
        x *= 0xFF51AFD7ED558CCDL;
        x ^= (x >>> 33);
        return (int) x;
    }
}

