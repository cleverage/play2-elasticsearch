package com.github.cleverage.elasticsearch;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Hossein Kazemi
 */
public class IndexUtilsTest {
    @Test
    public void shouldConvertValue() throws Exception {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        UUID conversionResult= (UUID) IndexUtils.convertValue(uuidString, UUID.class);
        assertEquals(uuid, conversionResult);
    }
}
