package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.ImpositionTotalsDto;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.repository.ImpositionRepository;

@ExtendWith(MockitoExtension.class)
class ImpositionServiceTest {

    @Mock
    private ImpositionRepository impositionRepository;
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