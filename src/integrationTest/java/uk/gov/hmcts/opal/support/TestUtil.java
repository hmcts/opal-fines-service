package uk.gov.hmcts.opal.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

public final class TestUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private TestUtil() {
    }

    @SneakyThrows
    public static String toJsonString(Object value) {
        return MAPPER.writeValueAsString(value);
    }
}
