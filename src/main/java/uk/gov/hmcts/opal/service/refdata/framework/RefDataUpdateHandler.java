package uk.gov.hmcts.opal.service.refdata.framework;

import java.util.Optional;

public interface RefDataUpdateHandler<T, E> {
    String refDataType();

    Class<T> payloadType();

    // Implementations should throw InvalidRefDataException for validation erorrs.
    void validateDto(T dto);

    Optional<E> findEntity(T dto);

    E createEntity(T dto);

    E saveEntity(E entity);

    RefDataUpdateMapper<T, E> mapper();
}
