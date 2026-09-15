package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;

public interface JsonNullableMapper {

    @Named("toJsonNullable")
    default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }
}
