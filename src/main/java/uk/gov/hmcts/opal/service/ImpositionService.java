package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.entity.CreditorTransactionType.FCC;
import static uk.gov.hmcts.opal.entity.CreditorTransactionType.FCOMP;
import static uk.gov.hmcts.opal.entity.CreditorTransactionType.FCOST;
import static uk.gov.hmcts.opal.entity.CreditorTransactionType.FCPC;
import static uk.gov.hmcts.opal.entity.CreditorTransactionType.FO;
import static uk.gov.hmcts.opal.entity.CreditorTransactionType.FVS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.ImpositionTotalsDto;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.repository.ImpositionRepository;

@Service
@AllArgsConstructor
public class ImpositionService {

    private static final String FO_VALUE = FO.getValue();
    private static final String FCOMP_VALUE = FCOMP.getValue();
    private static final String FCC_VALUE = FCC.getValue();
    private static final String FVS_VALUE = FVS.getValue();
    private static final String FCPC_VALUE = FCPC.getValue();
    private static final String FCOST_VALUE = FCOST.getValue();

    private static final Set<String> OPERATIONAL_REPORT_OTHER_IMPOSITION_EXCLUSIONS = Set.of(
        FCPC_VALUE,
        FCOST_VALUE,
        FO_VALUE,
        FCOMP_VALUE,
        FCC_VALUE,
        FVS_VALUE
    );

    private final ImpositionRepository impositionRepository;

    public ImpositionTotalsDto getAccountImpositionTotals(long defendantAccountId) {
        List<ImpositionEntity> impositions =
            impositionRepository.findAllByDefendantAccountId(defendantAccountId);
        if (impositions.isEmpty()) {
            return ImpositionTotalsDto.builder().build();
        }
        Map<String, BigDecimal> totalsByResultId = impositions.stream()
            .collect(Collectors.groupingBy(
                ImpositionEntity::getResultId,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    imposition -> Optional.ofNullable(imposition.getImposedAmount())
                        .orElse(BigDecimal.ZERO),
                    BigDecimal::add
                )
            ));

        return ImpositionTotalsDto.builder()
            .fineImpositions(totalsByResultId.getOrDefault(FO_VALUE, BigDecimal.ZERO))
            .costImpositions(calculateCostImposition(totalsByResultId))
            .compensationImpositions(totalsByResultId.getOrDefault(FCOMP_VALUE, BigDecimal.ZERO))
            .criminalCourtsChargeImpositions(totalsByResultId.getOrDefault(FCC_VALUE, BigDecimal.ZERO))
            .victimSurchargeImpositions(totalsByResultId.getOrDefault(FVS_VALUE, BigDecimal.ZERO))
            .otherImpositions(calculateOperationalReportOtherImposition(totalsByResultId))
            .build();
    }

    private BigDecimal calculateCostImposition(Map<String, BigDecimal> totalsByResultId) {
        return totalsByResultId.getOrDefault(FCPC_VALUE, BigDecimal.ZERO)
            .add(totalsByResultId.getOrDefault(FCOST_VALUE, BigDecimal.ZERO));
    }

    private BigDecimal calculateOperationalReportOtherImposition(Map<String, BigDecimal> totalsByResultId) {
        return totalsByResultId.entrySet().stream()
            .filter(entry -> !OPERATIONAL_REPORT_OTHER_IMPOSITION_EXCLUSIONS.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public LocalDate getEarliestImpositionDate(Long defendantAccountId) {
        ImpositionEntity entity =
            impositionRepository.findFirstByDefendantAccountIdOrderByImposedDateAsc(defendantAccountId);
        if (entity == null || entity.getImposedDate() == null) {
            return null;
        }
        return entity.getImposedDate().toLocalDate();
    }
}