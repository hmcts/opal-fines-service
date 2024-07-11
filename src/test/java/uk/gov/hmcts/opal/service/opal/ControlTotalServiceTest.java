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
import uk.gov.hmcts.opal.dto.search.ControlTotalSearchDto;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;
import uk.gov.hmcts.opal.repository.ControlTotalRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControlTotalServiceTest {

    @Mock
    private ControlTotalRepository controlTotalRepository;

    @InjectMocks
    private ControlTotalService controlTotalService;

    @Test
    void testGetControlTotal() {
        // Arrange

        ControlTotalEntity controlTotalEntity = ControlTotalEntity.builder().build();
        when(controlTotalRepository.getReferenceById(any())).thenReturn(controlTotalEntity);

        // Act
        ControlTotalEntity result = controlTotalService.getControlTotal(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchControlTotals() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        ControlTotalEntity controlTotalEntity = ControlTotalEntity.builder().build();
        Page<ControlTotalEntity> mockPage = new PageImpl<>(List.of(controlTotalEntity), Pageable.unpaged(), 999L);
        when(controlTotalRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<ControlTotalEntity> result = controlTotalService.searchControlTotals(ControlTotalSearchDto
                                                                                      .builder().build());

        // Assert
        assertEquals(List.of(controlTotalEntity), result);

    }


}
