package com.vvd.test.infrastructure.dcc;

import com.vvd.infrastructure.dcc.DCCService;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DCCServiceTest {

    @Test
    public void cutRangeUsesAnExclusiveUpperBound() {
        DCCService service = new DCCService();
        ReflectionTestUtils.setField(service, "cutRange", "0");
        assertFalse(service.isCutRange("d")); // 桶 0
        assertFalse(service.isCutRange("polygenelubricants")); // hashCode 为 Integer.MIN_VALUE

        ReflectionTestUtils.setField(service, "cutRange", "50");
        assertTrue(service.isCutRange("1"));  // 桶 49
        assertFalse(service.isCutRange("2")); // 桶 50

        ReflectionTestUtils.setField(service, "cutRange", "100");
        assertTrue(service.isCutRange("c"));  // 桶 99
    }
}
