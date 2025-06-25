package uk.gov.hmcts.opal.entity;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountSummary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class DefendantAccountSummaryTest {

    @Test
    void testDefaultMethod() {

        DefendantAccountSummary.PartyDefendantAccountSummary mockSummary =
            Mockito.mock(DefendantAccountSummary.PartyDefendantAccountSummary.class);

        when(mockSummary.getTitle()).thenReturn("Mr");
        when(mockSummary.getForenames()).thenReturn("JJ");
        when(mockSummary.getSurname()).thenReturn("Smith");
        when(mockSummary.getFullName()).thenCallRealMethod();

        assertEquals("Mr JJ Smith", mockSummary.getFullName());
    }

}
