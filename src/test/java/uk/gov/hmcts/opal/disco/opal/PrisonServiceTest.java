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
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.repository.PrisonRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrisonServiceTest {

    @Mock
    private PrisonRepository prisonRepository;

    @InjectMocks
    private PrisonService prisonService;

    @Test
    void testGetPrison() {
        // Arrange

        PrisonEntity prisonEntity = PrisonEntity.builder().build();
        when(prisonRepository.getReferenceById(any())).thenReturn(prisonEntity);

        // Act
        PrisonEntity result = prisonService.getPrison(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchPrisons() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        PrisonEntity prisonEntity = PrisonEntity.builder().build();
        Page<PrisonEntity> mockPage = new PageImpl<>(List.of(prisonEntity), Pageable.unpaged(), 999L);
        when(prisonRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<PrisonEntity> result = prisonService.searchPrisons(PrisonSearchDto.builder().build());

        // Assert
        assertEquals(List.of(prisonEntity), result);

    }


}
