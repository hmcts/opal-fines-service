package uk.gov.hmcts.opal.entity;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface FullNameBuilder {

    String getTitle();

    String getForenames();

    String getSurname();

    default String getFullName() {
        return Stream.of(getTitle(), getForenames(), getSurname())
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
    }

}
