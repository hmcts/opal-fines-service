package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.ImpositionTotalsDto;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.service.proxy.ImpositionServiceProxy;

@ExtendWith(MockitoExtension.class)
class ImpositionServiceTest {

    @Mock
    private ImpositionRepository impositionRepository;

    @Mock
    private UserStateService userStateService;

    @Mock
    private UserState userState;

    @Mock
    private ImpositionServiceProxy impositionServiceProxy;

    @InjectMocks
    private ImpositionService impositionService;

    @Test
    void shouldCalculateTotalsByResultIdForAccountImposition() {
        List<ImpositionEntity> impositions = new ArrayList<>();
        addImpositions(impositions, "FCOMP", 2);
        addImpositions(impositions, "FCPC", 1);
        addImpositions(impositions, "FCOST", 2);
        addImpositions(impositions, "FO", 4);
        addImpositions(impositions, "FVS", 5);
        addImpositions(impositions, "FCC", 6);
        for (int i = 0; i < 7; i++) {
            impositions.add(buildImpositionEntityWith50PoundImposition("ABC" + i));
        }
        when(impositionRepository.findAllByDefendantAccountId(1L)).thenReturn(impositions);

        ImpositionTotalsDto result = impositionService.getAccountImpositionTotals(1L);

        assertThat(result.getCompensationImpositions()).isEqualByComparingTo("100.00");
        assertThat(result.getCostImpositions()).isEqualByComparingTo("150.00");
        assertThat(result.getFineImpositions()).isEqualByComparingTo("200.00");
        assertThat(result.getVictimSurchargeImpositions()).isEqualByComparingTo("250.00");
        assertThat(result.getCriminalCourtsChargeImpositions()).isEqualByComparingTo("300.00");
        assertThat(result.getOtherImpositions()).isEqualByComparingTo("350.00");
    }

    @Test
    void getEarliestImpositionDate_returnsLocalDateWhenImposedDateExists() {
        Long defendantAccountId = 1L;
        ImpositionEntity entity = new ImpositionEntity();
        entity.setImposedDate(LocalDateTime.of(2025, 5, 15, 10, 30));
        when(impositionRepository.findFirstByDefendantAccountIdOrderByImposedDateAsc(defendantAccountId))
            .thenReturn(entity);

        LocalDate result = impositionService.getEarliestImpositionDate(defendantAccountId);

        assertThat(result).isEqualTo(LocalDate.of(2025, 5, 15));
    }

    @Test
    void getEarliestImpositionDate_returnsNullWhenImposedDateIsNull() {
        Long defendantAccountId = 1L;
        ImpositionEntity entity = new ImpositionEntity();
        entity.setImposedDate(null);
        when(impositionRepository.findFirstByDefendantAccountIdOrderByImposedDateAsc(defendantAccountId))
            .thenReturn(entity);

        LocalDate result = impositionService.getEarliestImpositionDate(defendantAccountId);

        assertThat(result).isNull();
    }

    @Test
    void getEarliestImpositionDate_returnsNullWhenEntityIsNull() {
        Long defendantAccountId = 1L;
        when(impositionRepository.findFirstByDefendantAccountIdOrderByImposedDateAsc(defendantAccountId))
            .thenReturn(null);

        LocalDate result = impositionService.getEarliestImpositionDate(defendantAccountId);

        assertThat(result).isNull();
    }

    @Test
    void getImpositions_whenUserHasPermission_returnsImpositionsServiceResult() {
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        GetDefendantAccountImpositionsResponse impositionsResponse = GetDefendantAccountImpositionsResponse.builder()
            .version(BigInteger.valueOf(9))
            .build();
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(impositionServiceProxy.getImpositions(defendantAccountId)).thenReturn(impositionsResponse);

        GetDefendantAccountImpositionsResponse result =
            impositionService.getImpositions(defendantAccountId);

        assertSame(impositionsResponse, result);
        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verify(impositionServiceProxy).getImpositions(defendantAccountId);
        verifyNoMoreInteractions(userStateService, userState, impositionServiceProxy);
    }

    @Test
    void getImpositions_whenUserLacksPermission_throwsPermissionNotAllowed() {
        Long defendantAccountId = 77L;
        String authHeader = "Bearer abc";
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(false);

        assertThrows(
            PermissionNotAllowedException.class,
            () -> impositionService.getImpositions(defendantAccountId)
        );

        verify(userStateService).getUserStateV1FromSecurityContext();
        verify(userState).anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        verifyNoInteractions(impositionServiceProxy);
        verifyNoMoreInteractions(userStateService, userState);
    }

    private ImpositionEntity buildImpositionEntityWith50PoundImposition(String resultId) {
        return ImpositionEntity.builder()
            .resultId(resultId)
            .imposedAmount(BigDecimal.valueOf(50.00))
            .build();
    }

    private void addImpositions(List<ImpositionEntity> impositions, String code, int count) {
        for (int i = 0; i < count; i++) {
            impositions.add(buildImpositionEntityWith50PoundImposition(code));
        }
    }
}