package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles({"integration", "opal"})
@Slf4j
abstract class AbstractOpalEnforcementAccountTypesIntegrationTest extends AbstractIntegrationWithSecurityTest {

    protected static final String URL_BASE = "/enforcement-accounts-types/";

    @MockitoBean
    protected UserStateService userStateService;

    @MockitoSpyBean
    protected JsonSchemaValidationService jsonSchemaValidationService;

    @MockitoBean
    UserStateClientService userStateClientService;

    @MockitoBean
    protected UserState userState;

    @MockitoBean
    protected AccessTokenService accessTokenService;

    @BeforeEach
    void setupUserState() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any())).thenReturn(true);
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any())).thenReturn(userState);
    }

    protected void authorizeWithPermission(short businessUnitId) {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(businessUnitId, FinesPermission.AUTO_ENFORCEMENT);
    }

    protected void authoriseNoPermissions() {
        userStateStub.setupWithNoPermissions();
    }
}
