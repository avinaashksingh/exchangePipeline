package com.scetzhbook.exchangePipeline.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.scetzhbook.exchangePipeline.model.Side;

class OrderIdGeneratorTest {

    private OrderIdGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new OrderIdGenerator();
    }

    @Test
    void nextGeneratesNonNullId() {
        String id = generator.next("TST", Side.BUY, 100.0, 10, System.currentTimeMillis());
        assertNotNull(id);
    }

    @Test
    void nextGeneratesDifferentIdsForDifferentInputs() {
        long now = System.currentTimeMillis();
        String id1 = generator.next("TST", Side.BUY, 100.0, 10, now);
        String id2 = generator.next("TST", Side.SELL, 100.0, 10, now);
        assertNotEquals(id1, id2);
    }

    @Test
    void nextGeneratesDifferentIdsForSameInputsInSameMillisecond() {
        long now = System.currentTimeMillis();
        String id1 = generator.next("TST", Side.BUY, 100.0, 10, now);
        String id2 = generator.next("TST", Side.BUY, 100.0, 10, now);
        assertNotEquals(id1, id2);
    }

    @Test
    void idsAreStableForSameInputsAtDifferentTimes() {
        // The suffix is a hash of attributes, so it should be the same if attributes are the same
        long now = System.currentTimeMillis();
        String id1 = generator.next("TST", Side.BUY, 100.0, 10, now);
        String id2 = generator.next("TST", Side.BUY, 100.0, 10, now + 1000);
        
        String suffix1 = id1.substring(id1.indexOf('-') + 1);
        String suffix2 = id2.substring(id2.indexOf('-') + 1);
        
        assertEquals(suffix1, suffix2);
    }
}
