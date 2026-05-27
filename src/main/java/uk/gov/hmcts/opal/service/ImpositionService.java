package uk.gov.hmcts.opal.service;

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

    private final ImpositionRepository impositionRepository;

    public ImpositionTotalsDto getAccountImpositionTotals(long defendantAccountId) {
        List<ImpositionEntity> impositions = impositionRepository.findAllByDefendantAccountId(defendantAccountId);
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
            .fineImpositions(totalsByResultId.getOrDefault("FO", BigDecimal.ZERO))
            .costImpositions(calculateCostImposition(totalsByResultId))
            .compensationImpositions(totalsByResultId.getOrDefault("FCOMP", BigDecimal.ZERO))
            .criminalCourtsChargeImpositions(totalsByResultId.getOrDefault("FCC", BigDecimal.ZERO))
            .victimSurchargeImpositions(totalsByResultId.getOrDefault("FVS", BigDecimal.ZERO))
            .otherImpositions(calculateOtherImposition(totalsByResultId))
            .build();
    }

    private BigDecimal calculateCostImposition(Map<String, BigDecimal> totalsByResultId) {
        return totalsByResultId.getOrDefault("FCPC", BigDecimal.ZERO)
            .add(totalsByResultId.getOrDefault("FCOST", BigDecimal.ZERO));
    }

    private BigDecimal calculateOtherImposition(Map<String, BigDecimal> totalsByResultId) {
        Set<String> excluded = Set.of("FCPC", "FCOST", "FO", "FCOMP", "FCC", "FVS");

        return totalsByResultId.entrySet().stream()
            .filter(entry -> !excluded.contains(entry.getKey()))
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


