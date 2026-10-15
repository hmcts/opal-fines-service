package uk.gov.hmcts.opal.dto;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;

class PdplIdentifierTypeTest {

    @ParameterizedTest
    @EnumSource(PdplIdentifierType.class)
    void shouldConstructParticipantIdentifierUsingDatabaseEnumName(PdplIdentifierType identifierType) {
        ParticipantIdentifier participant =
            ParticipantIdentifier.builder().identifier("123").type(identifierType).build();

        assertThat(participant.getType()).isSameAs(identifierType);
    }

    @Test
    void shouldMatchLoggingServiceDatabaseEnumValues() {
        assertThat(Arrays.stream(PdplIdentifierType.values()).map(PdplIdentifierType::getType)).containsExactly(
            "DEFENDANT_ACCOUNT", "CONSOLIDATED_ACCOUNT", "DEBTOR_ACCOUNT", "PARENT_GUARDIAN", "DRAFT_ACCOUNT", "PAYER",
            "OPAL_USER_ID", "EXTERNAL_SYSTEM", "MINOR_CREDITOR");
    }
}