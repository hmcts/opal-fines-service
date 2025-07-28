package uk.gov.hmcts.opal.disco.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FixedPenaltyOffenceServiceTest {

    @Mock
    private FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;

    @InjectMocks
    private FixedPenaltyOffenceService fixedPenaltyOffenceService;

    @Test
    void testGetFixedPenaltyOffence() {
        // Arrange

        FixedPenaltyOffenceEntity fixedPenaltyOffenceEntity = FixedPenaltyOffenceEntity.builder().build();
        when(fixedPenaltyOffenceRepository.getReferenceById(any())).thenReturn(fixedPenaltyOffenceEntity);

        // Act
        FixedPenaltyOffenceEntity result = fixedPenaltyOffenceService.getFixedPenaltyOffence(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchFixedPenaltyOffences() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        FixedPenaltyOffenceEntity fixedPenaltyOffenceEntity = FixedPenaltyOffenceEntity.builder().build();
        Page<FixedPenaltyOffenceEntity> mockPage = new PageImpl<>(List.of(fixedPenaltyOffenceEntity),
                                                                  Pageable.unpaged(), 999L);
        when(fixedPenaltyOffenceRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<FixedPenaltyOffenceEntity> result = fixedPenaltyOffenceService.searchFixedPenaltyOffences(
            FixedPenaltyOffenceSearchDto.builder().build());

        // Assert
        assertEquals(List.of(fixedPenaltyOffenceEntity), result);

    }


}
