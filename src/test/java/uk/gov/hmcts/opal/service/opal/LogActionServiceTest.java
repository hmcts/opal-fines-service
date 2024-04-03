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
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.repository.LogActionRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogActionServiceTest {

    @Mock
    private LogActionRepository logActionRepository;

    @InjectMocks
    private LogActionService logActionService;

    @Test
    void testGetLogAction() {
        // Arrange

        LogActionEntity logActionEntity = LogActionEntity.builder().build();
        when(logActionRepository.getReferenceById(any())).thenReturn(logActionEntity);

        // Act
        LogActionEntity result = logActionService.getLogAction((short)1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchLogActions() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        LogActionEntity logActionEntity = LogActionEntity.builder().build();
        Page<LogActionEntity> mockPage = new PageImpl<>(List.of(logActionEntity), Pageable.unpaged(), 999L);
        when(logActionRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<LogActionEntity> result = logActionService.searchLogActions(LogActionSearchDto.builder().build());

        // Assert
        assertEquals(List.of(logActionEntity), result);

    }


}
