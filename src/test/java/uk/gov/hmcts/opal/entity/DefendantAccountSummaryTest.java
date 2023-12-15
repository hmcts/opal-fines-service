package uk.gov.hmcts.opal.entity;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class DefendantAccountSummaryTest {

    @Test
    public void testDefaultMethod() {

        DefendantAccountSummary.PartyDefendantAccountSummary mockSummary =
            Mockito.mock(DefendantAccountSummary.PartyDefendantAccountSummary.class);

        when(mockSummary.getTitle()).thenReturn("Mr");
        when(mockSummary.getForenames()).thenReturn("JJ");
        when(mockSummary.getSurname()).thenReturn("Smith");
        when(mockSummary.getName()).thenCallRealMethod();

        assertEquals("Mr JJ Smith", mockSummary.getName());
    }

}
