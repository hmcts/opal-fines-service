package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.service.opal.MajorCreditorHistoryPdplLoggingService;
import uk.gov.hmcts.opal.service.proxy.MajorCreditorAccountProxy;

@ExtendWith(MockitoExtension.class)
class MajorCreditorAccountServiceTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private MajorCreditorAccountProxy majorCreditorAccountProxy;

    @Mock
    private MajorCreditorHistoryPdplLoggingService pdplLoggingService;

    @InjectMocks
    private MajorCreditorAccountService majorCreditorAccountService;

    @Nested
    class GetAtAGlance {

        @Test
        void whenUserIsAuthorised_delegatesToProxy_happyPath() {
            UserStateV2 userState = mock(UserStateV2.class);
            GetMajorCreditorAccountAtAGlanceResponse response = new GetMajorCreditorAccountAtAGlanceResponse();

            when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
            when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
                .thenReturn(true);
            when(majorCreditorAccountProxy.getAtAGlance(123L)).thenReturn(response);

            GetMajorCreditorAccountAtAGlanceResponse result = majorCreditorAccountService.getAtAGlance(123L);

            assertEquals(response, result);
            verify(userStateService).getUserStateFromSecurityContext();
            verify(majorCreditorAccountProxy).getAtAGlance(123L);
        }

        @Test
        void whenUserLacksPermission_throwsForbidden_sadPath() {
            UserStateV2 userState = mock(UserStateV2.class);
            when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
                .thenReturn(false);
            when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);

            PermissionNotAllowedException exception = assertThrows(
                PermissionNotAllowedException.class,
                () -> majorCreditorAccountService.getAtAGlance(123L)
            );

            assertAll(
                () -> assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS),
                () -> verifyNoInteractions(majorCreditorAccountProxy)
            );
        }
    }

    @Nested
    class GetHeaderSummary {

        @Test
        void whenUserIsAuthorised_delegatesToProxy_happyPath() {
            UserStateV2 userState = mock(UserStateV2.class);
            GetMajorCreditorAccountHeaderSummaryResponse response = responseWithBusinessUnit((short) 77);

            when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
            when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
                .thenReturn(true);
            when(userState.hasBusinessUnitUserWithPermission((short) 77,
                FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
            when(majorCreditorAccountProxy.getHeaderSummary(123L)).thenReturn(response);

            GetMajorCreditorAccountHeaderSummaryResponse result =
                majorCreditorAccountService.getHeaderSummary(123L);

            assertEquals(response, result);
            verify(userStateService).getUserStateFromSecurityContext();
            verify(userState).hasBusinessUnitUserWithPermission((short) 77,
                FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
            verify(majorCreditorAccountProxy).getHeaderSummary(123L);
        }

        @Test
        void whenUserLacksPermission_throwsForbidden_sadPath() {
            UserStateV2 userState = mock(UserStateV2.class);
            when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
                .thenReturn(false);
            when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);

            PermissionNotAllowedException exception = assertThrows(
                PermissionNotAllowedException.class,
                () -> majorCreditorAccountService.getHeaderSummary(123L)
            );

            assertAll(
                () -> assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS),
                () -> verifyNoInteractions(majorCreditorAccountProxy)
            );
        }

        @Test
        void whenPermissionIsForDifferentBusinessUnit_throwsForbidden_sadPath() {
            UserStateV2 userState = mock(UserStateV2.class);
            GetMajorCreditorAccountHeaderSummaryResponse response = responseWithBusinessUnit((short) 77);

            when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
            when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
                .thenReturn(true);
            when(userState.hasBusinessUnitUserWithPermission((short) 77,
                FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);
            when(majorCreditorAccountProxy.getHeaderSummary(123L)).thenReturn(response);

            PermissionNotAllowedException exception = assertThrows(
                PermissionNotAllowedException.class,
                () -> majorCreditorAccountService.getHeaderSummary(123L)
            );

            assertAll(
                () -> assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS),
                () -> assertThat(exception.getBusinessUnitId()).isEqualTo((short) 77),
                () -> verify(majorCreditorAccountProxy).getHeaderSummary(123L)
            );
        }
    }

    @Nested
    class GetHistory {

        @Test
        void whenUserIsAuthorised_queuesPdpoThenReturnsResponse_happyPath() {
            UserStateV2 userState = mock(UserStateV2.class);
            LocalDate dateFrom = LocalDate.of(2026, Month.JANUARY, 1);
            LocalDate dateTo = LocalDate.of(2026, Month.JANUARY, 31);
            List<String> itemTypes = List.of("financial");
            GetMajorCreditorHistoryResponse response = GetMajorCreditorHistoryResponse.builder().build();

            when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
            when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
                .thenReturn(true);
            when(majorCreditorAccountProxy.getHistory(123L, dateFrom, dateTo, itemTypes)).thenReturn(response);

            GetMajorCreditorHistoryResponse result =
                majorCreditorAccountService.getHistory(123L, dateFrom, dateTo, itemTypes);

            InOrder responseOrder = inOrder(majorCreditorAccountProxy, pdplLoggingService);
            assertEquals(response, result);
            responseOrder.verify(majorCreditorAccountProxy).getHistory(123L, dateFrom, dateTo, itemTypes);
            responseOrder.verify(pdplLoggingService).logHistoryItems(userState, response);
        }

        @Test
        void whenPdpoLoggingFails_propagatesFailure_sadPath() {
            UserStateV2 userState = mock(UserStateV2.class);
            GetMajorCreditorHistoryResponse response = GetMajorCreditorHistoryResponse.builder().build();
            PdplLoggingException loggingFailure = new PdplLoggingException("queue unavailable");
            mockAuthorisedHistory(userState, response);
            doThrow(loggingFailure).when(pdplLoggingService).logHistoryItems(userState, response);

            PdplLoggingException exception = assertThrows(
                PdplLoggingException.class,
                () -> majorCreditorAccountService.getHistory(123L, null, null, null)
            );

            assertAll(
                () -> assertThat(exception).isSameAs(loggingFailure),
                () -> verify(pdplLoggingService).logHistoryItems(userState, response)
            );
        }

        @Test
        void whenUserLacksPermission_throwsForbidden_sadPath() {
            UserStateV2 userState = mock(UserStateV2.class);
            when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
                .thenReturn(false);
            when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);

            PermissionNotAllowedException exception = assertThrows(
                PermissionNotAllowedException.class,
                () -> majorCreditorAccountService.getHistory(123L, null, null, null)
            );

            assertAll(
                () -> assertThat(exception.getPermission()).containsExactly(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS),
                () -> verifyNoInteractions(majorCreditorAccountProxy, pdplLoggingService)
            );
        }

        @Test
        void whenDateRangeIsInvalid_throwsBadRequestBeforeProxyCall_sadPath() {
            UserStateV2 userState = mock(UserStateV2.class);
            when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
            when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
                .thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> majorCreditorAccountService.getHistory(
                    123L,
                    LocalDate.of(2026, 1, 31),
                    LocalDate.of(2026, 1, 1),
                    null
                )
            );

            assertAll(
                () -> assertEquals("dateFrom must be on or before dateTo", exception.getMessage()),
                () -> verifyNoInteractions(majorCreditorAccountProxy, pdplLoggingService)
            );
        }

        @Test
        void whenItemTypeIsUnsupported_throwsBadRequestBeforeProxyCall_sadPath() {
            UserStateV2 userState = mock(UserStateV2.class);
            when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
            when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
                .thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> majorCreditorAccountService.getHistory(123L, null, null, List.of("amendment"))
            );

            assertAll(
                () -> assertEquals("itemTypes must contain only financial, note", exception.getMessage()),
                () -> verifyNoInteractions(majorCreditorAccountProxy, pdplLoggingService)
            );
        }
    }

    private void mockAuthorisedHistory(UserStateV2 userState, GetMajorCreditorHistoryResponse response) {
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(majorCreditorAccountProxy.getHistory(123L, null, null, null)).thenReturn(response);
    }

    private GetMajorCreditorAccountHeaderSummaryResponse responseWithBusinessUnit(Short businessUnitId) {
        GetMajorCreditorAccountHeaderSummaryResponse response =
            new GetMajorCreditorAccountHeaderSummaryResponse();
        response.setBusinessUnitDetails(new BusinessUnitSummaryCommon().businessUnitId(businessUnitId));
        return response;
    }
}
