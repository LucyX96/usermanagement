package com.company.usermanagement;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class TestJson {
    private TestJson() {}

    public static String toJson(ObjectMapper mapper, Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
