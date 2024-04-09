package uk.gov.hmcts.opal.service.opal;

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
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.ImpositionEntity;
import uk.gov.hmcts.opal.repository.ImpositionRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpositionServiceTest {

    @Mock
    private ImpositionRepository impositionRepository;

    @InjectMocks
    private ImpositionService impositionService;

    @Test
    void testGetImposition() {
        // Arrange

        ImpositionEntity impositionEntity = ImpositionEntity.builder().build();
        when(impositionRepository.getReferenceById(any())).thenReturn(impositionEntity);

        // Act
        ImpositionEntity result = impositionService.getImposition(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchImpositions() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        ImpositionEntity impositionEntity = ImpositionEntity.builder().build();
        Page<ImpositionEntity> mockPage = new PageImpl<>(List.of(impositionEntity), Pageable.unpaged(), 999L);
        when(impositionRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<ImpositionEntity> result = impositionService.searchImpositions(ImpositionSearchDto.builder().build());

        // Assert
        assertEquals(List.of(impositionEntity), result);

    }


}
