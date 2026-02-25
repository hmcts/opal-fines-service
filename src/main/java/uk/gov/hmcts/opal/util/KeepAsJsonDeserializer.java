package uk.gov.hmcts.opal.util;


import tools.jackson.core.JsonParser;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class KeepAsJsonDeserializer extends ValueDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) {
        TreeNode tree = jp.objectReadContext().readTree(jp);
        return tree.toString();
    }
}
