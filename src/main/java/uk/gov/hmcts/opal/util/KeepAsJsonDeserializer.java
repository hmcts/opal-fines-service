package uk.gov.hmcts.opal.util;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class KeepAsJsonDeserializer extends StdDeserializer<String> {

    public KeepAsJsonDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
        throws JacksonException {

        return jp.readValueAsTree().toString();
    }
}
