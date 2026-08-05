package uk.gov.hmcts.opal.controllers;

import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;
import uk.gov.hmcts.opal.service.opal.DefendantAccountDeletionService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;
import uk.gov.hmcts.opal.service.opal.OpalCreditorAccountService;
import uk.gov.hmcts.opal.service.DraftAccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
    classes =
        {
            TestingSupportController.class,
            DynamicConfigService.class,
            FeatureToggleApi.class,
            DefendantAccountDeletionService.class
        },
    properties = {
        "opal.testing-support-endpoints.enabled=true"
    }
)
@Isolated
@SuppressWarnings("java:S1874")
class TestingSupportControllerTest {

    @Autowired
    private TestingSupportController controller;

    @MockitoBean
    private DynamicConfigService configService;

    @MockitoBean
    private FeatureToggleApi featureToggleApi;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private DefendantAccountDeletionService defendantAccountDeletionService;

    @MockitoBean
    private UserStateClientService userStateClientService;

    @MockitoBean
    private UserStateMapper userStateMapper;

    @MockitoBean
    private MajorCreditorService majorCreditorService;

    @MockitoBean
    private BusinessUnitService businessUnitService;

    @MockitoBean
    private OpalCreditorAccountService opalCreditorAccountService;

    @MockitoBean
    private DraftAccountService draftAccountService;

    @MockitoBean
    private LocalJusticeAreaService localJusticeAreaService;

    @Test
    void isLegacyMode() {
        when(configService.isLegacyMode()).thenReturn(false);

        ResponseEntity<Boolean> response = controller.isLegacyMode();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().booleanValue());
    }

    @Test
    void isFeatureEnabled() {
        when(featureToggleApi.isFeatureEnabled("my-feature")).thenReturn(true);

        ResponseEntity<Boolean> response = controller.isFeatureEnabled("my-feature");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    void getFeatureFlagValue() {
        when(featureToggleApi.getFeatureValue("my-feature", "")).thenReturn("value");

        ResponseEntity<String> response = controller.getFeatureValue("my-feature");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("value", response.getBody());
    }

    @Test
    void parseToken_shouldReturnEmail() {
        String bearerToken = "Bearer token";
        when(accessTokenService.extractPreferredUsername(bearerToken)).thenReturn("my@email.com");

        ResponseEntity<String> response = controller.parseToken(bearerToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("my@email.com", response.getBody());
    }

    @Test
    void getUserState_shouldReturnResponse() {
        UserState userState = UserStateUtil.permissionUser((short) 1, FinesPermission.ACCOUNT_ENQUIRY);
        UserStateV2 userStateV2 = mock(UserStateV2.class);
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.of(userStateV2));
        when(userStateMapper.toUserState(userStateV2, Domain.FINES)).thenReturn(userState);

        ResponseEntity<UserState> response = controller.getUserState(0L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userState, response.getBody());
    }

    @Test
    void getUserState_shouldReturnNotFound() {
        ResponseEntity<UserState> response = controller.getUserState(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.hasBody());
    }

    @Test
    void deleteDefendantAccountWithAllData() {
        ResponseEntity<Void> response = controller.deleteDefendantAccountWithAllData(123L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(response.hasBody());
        verify(defendantAccountDeletionService).deleteDefendantAccountAndAssociatedData(123L);
    }

    @Test
    void postMajorCreditorsSearch() {
        List<MajorCreditorEntity> majorCreditors = List.of(MajorCreditorEntity.builder().build());
        MajorCreditorSearchDto criteria = MajorCreditorSearchDto.builder().build();
        when(majorCreditorService.searchMajorCreditors(criteria)).thenReturn(majorCreditors);

        ResponseEntity<List<MajorCreditorEntity>> response = controller.postMajorCreditorsSearch(criteria);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(majorCreditors, response.getBody());
        verify(majorCreditorService).searchMajorCreditors(criteria);
    }

    @Test
    void postBusinessUnitsSearch() {
        List<BusinessUnitEntity> businessUnits = List.of(BusinessUnitEntity.builder().build());
        BusinessUnitSearchDto criteria = BusinessUnitSearchDto.builder().build();
        when(businessUnitService.searchBusinessUnits(criteria)).thenReturn(businessUnits);

        ResponseEntity<List<BusinessUnitEntity>> response = controller.postBusinessUnitsSearch(criteria);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(businessUnits, response.getBody());
        verify(businessUnitService).searchBusinessUnits(criteria);
    }

    @Test
    void deleteMinorCreditorById() {
        when(opalCreditorAccountService.deleteCreditorAccount(123L, true)).thenReturn("OK");

        ResponseEntity<String> response = controller.deleteMinorCreditorById(123L, "if-match",
            Optional.of(false));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
        verify(opalCreditorAccountService).deleteCreditorAccount(123L, true);
    }

    @Test
    void postDraftAccountsSearch() {
        List<DraftAccountResponseDto> draftAccounts = List.of(DraftAccountResponseDto.builder().build());
        DraftAccountSearchDto criteria = DraftAccountSearchDto.builder().build();
        when(draftAccountService.searchDraftAccounts(criteria)).thenReturn(draftAccounts);

        ResponseEntity<List<DraftAccountResponseDto>> response = controller.postDraftAccountsSearch(criteria);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(draftAccounts, response.getBody());
        verify(draftAccountService).searchDraftAccounts(criteria);
    }

    @Test
    void deleteDraftAccountById() {
        when(draftAccountService.deleteDraftAccount(123L, true)).thenReturn("OK");

        ResponseEntity<String> response = controller.deleteDraftAccountById(123L, "if-match", Optional.of(false));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
        verify(draftAccountService).deleteDraftAccount(123L, true);
    }

    @Test
    void postLocalJusticeAreasSearch() {
        List<LocalJusticeAreaEntity> localJusticeAreas = List.of(LocalJusticeAreaEntity.builder().build());
        LocalJusticeAreaSearchDto criteria = LocalJusticeAreaSearchDto.builder().build();
        when(localJusticeAreaService.searchLocalJusticeAreas(criteria)).thenReturn(localJusticeAreas);

        ResponseEntity<List<LocalJusticeAreaEntity>> response = controller.postLocalJusticeAreasSearch(criteria);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(localJusticeAreas, response.getBody());
        verify(localJusticeAreaService).searchLocalJusticeAreas(criteria);
    }
}
