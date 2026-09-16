package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.DefendantAccountPaymentTermsService;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountControllerTest {

    @Mock
    private DefendantAccountService defendantAccountService;

    @Mock
    private DefendantAccountPaymentTermsService defendantAccountPaymentTermsService;

    @InjectMocks
    private DefendantAccountController defendantAccountController;

    @Test
    void testLegacyDefendantAccountService_createGetDefendantAccountRequest() {
        String id = "77";
        var req = LegacyDefendantAccountService.createGetDefendantAccountRequest(id);
        assertNotNull(req);
        assertEquals(id, req.getDefendantAccountId());
    }

}
