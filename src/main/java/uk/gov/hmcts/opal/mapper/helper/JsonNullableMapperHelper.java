package uk.gov.hmcts.opal.mapper.helper;

import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;

public final class JsonNullableMapperHelper {

    private JsonNullableMapperHelper() {
        // Utility class.
    }

    @Named("toJsonNullable")
    public static <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }

    @Named("toJsonNullableOrUndefined")
    public static <T> JsonNullable<T> toJsonNullableOrUndefined(T value) {
        return value == null ? JsonNullable.undefined() : JsonNullable.of(value);
    }

    @Named("fromJsonNullable")
    public static <T> T fromJsonNullable(JsonNullable<T> value) {
        return value == null ? null : value.orElse(null);
    }
}
