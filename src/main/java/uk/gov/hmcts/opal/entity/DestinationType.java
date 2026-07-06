package uk.gov.hmcts.opal.entity;

import lombok.Getter;

@Getter
public enum DestinationType {
    C("Court Fee"),
    F("Fines"),
    S("Suspense");

    private final String description;

    DestinationType(String description) {
        this.description = description;
    }
}
