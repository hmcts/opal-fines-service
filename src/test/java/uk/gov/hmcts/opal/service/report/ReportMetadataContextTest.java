package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;

class ReportMetadataContextTest {

    private ReportMetadataContext context;

    @BeforeEach
    void setUp() {
        context = new ReportMetadataContext();
    }

    @Test
    void shouldNotAddParticipant_whenIdentifierIsNull() {
        context.addParticipant(null, PdplIdentifierType.DEFENDANT_ACCOUNT);
        assertThat(context.getParticipants()).isEmpty();
    }

    @Test
    void shouldNotAddDuplicateParticipant_sameIdentifierAndType() {
        context.addParticipant("12345", PdplIdentifierType.DEFENDANT_ACCOUNT);
        context.addParticipant("12345", PdplIdentifierType.DEFENDANT_ACCOUNT);
        assertThat(context.getParticipants()).hasSize(1);
    }

    @Test
    void shouldAllowSameIdentifierDifferentType() {
        context.addParticipant("12345", PdplIdentifierType.DEFENDANT_ACCOUNT);
        context.addParticipant("12345", PdplIdentifierType.DEBTOR_ACCOUNT);
        assertThat(context.getParticipants()).hasSize(2);
    }

    @Test
    void shouldAllowDifferentIdentifierSameType() {
        context.addParticipant("12345", PdplIdentifierType.DEFENDANT_ACCOUNT);
        context.addParticipant("67890", PdplIdentifierType.DEFENDANT_ACCOUNT);
        assertThat(context.getParticipants()).hasSize(2);
    }

}