package uk.gov.hmcts.opal.mapper.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class HibernateJsonValueMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private HibernateJsonValueMapper() {
        // Utility class.
    }

    /**
     * Converts a Hibernate-hydrated JSON value into plain JSON-compatible Java values.
     *
     * <p>This is only for DB JSON columns whose stored shape is intentionally flexible, such as a value that may be
     * either a JSON object or JSON array. Prefer the existing simpler patterns elsewhere in the codebase when the JSON
     * shape is known: map the entity field as a String, a concrete DTO, or a typed collection.
     *
     * <p>Hibernate JSON mapping currently hydrates tree-shaped JSON fields via Jackson 2. The rest of the application
     * uses tools.jackson/Jackson 3 helpers such as ToJsonString, so do not pass this tree model across service,
     * response, or cache boundaries. Convert it to plain JSON-compatible Map/List/String/Number/Boolean values instead.
     *
     * @param value the Hibernate-hydrated Jackson 2 JSON value
     * @return null, Map, List, String, Number, or Boolean values safe for response/cache DTOs
     */
    public static Object toJsonCompatibleValue(JsonNode value) {
        return value == null ? null : OBJECT_MAPPER.convertValue(value, Object.class);
    }
}
