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
import uk.gov.hmcts.opal.dto.search.OffenseSearchDto;
import uk.gov.hmcts.opal.entity.OffenseEntity;
import uk.gov.hmcts.opal.repository.OffenseRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenseServiceTest {

    @Mock
    private OffenseRepository offenseRepository;

    @InjectMocks
    private OffenseService offenseService;

    @Test
    void testGetOffense() {
        // Arrange

        OffenseEntity offenseEntity = OffenseEntity.builder().build();
        when(offenseRepository.getReferenceById(any())).thenReturn(offenseEntity);

        // Act
        OffenseEntity result = offenseService.getOffense(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchOffenses() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        OffenseEntity offenseEntity = OffenseEntity.builder().build();
        Page<OffenseEntity> mockPage = new PageImpl<>(List.of(offenseEntity), Pageable.unpaged(), 999L);
        when(offenseRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<OffenseEntity> result = offenseService.searchOffenses(OffenseSearchDto.builder().build());

        // Assert
        assertEquals(List.of(offenseEntity), result);

    }


}
