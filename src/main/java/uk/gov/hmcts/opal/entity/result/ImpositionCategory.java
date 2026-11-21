package uk.gov.hmcts.opal.entity.result;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum ImpositionCategory {
    COMPENSATION("Compensation"),
    COSTS("Costs"),
    COURT_CHARGE("Court Charge"),
    CROWN_PROSECUTION_COSTS("Crown Prosecution Costs"),
    FINES("Fines"),
    LEGAL_AID("Legal Aid"),
    VICTIM_SURCHARGE("Victim Surcharge"),
    WITNESS_EXPENSES_AND_CENTRAL_FUND("Witness Expenses & Central Fund");

    private final String label;

    ImpositionCategory(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static ImpositionCategory getByLabel(String label) {
        return Arrays.stream(values())
            .filter(category -> category.label.equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown ImpositionCategory: " + label));
    }
}
