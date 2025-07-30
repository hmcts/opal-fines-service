package uk.gov.hmcts.opal.disco.opal;

import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.opal.authorisation.model.LogActions;
import uk.gov.hmcts.opal.dto.AddLogAuditDetailDto;
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.LogActionRepository;
import uk.gov.hmcts.opal.repository.LogAuditDetailRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogAuditDetailServiceTest {

    @Mock
    private LogAuditDetailRepository logAuditDetailRepository;

    @Mock
    private LogActionRepository logActionRepository;

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @InjectMocks
    private LogAuditDetailService logAuditDetailService;

    @Test
    void testGetLogAuditDetail() {
        // Arrange

        LogAuditDetailEntity logAuditDetailEntity = LogAuditDetailEntity.builder().build();
        when(logAuditDetailRepository.getReferenceById(any())).thenReturn(logAuditDetailEntity);

        // Act
        LogAuditDetailEntity result = logAuditDetailService.getLogAuditDetail(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchLogAuditDetails() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        LogAuditDetailEntity logAuditDetailEntity = LogAuditDetailEntity.builder().build();
        Page<LogAuditDetailEntity> mockPage = new PageImpl<>(List.of(logAuditDetailEntity), Pageable.unpaged(), 999L);
        when(logAuditDetailRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<LogAuditDetailEntity> result = logAuditDetailService.searchLogAuditDetails(
            LogAuditDetailSearchDto.builder().build());

        // Assert
        assertEquals(List.of(logAuditDetailEntity), result);

    }

    @Test
    void testWriteLogAuditDetail() {
        // Arrange
        AddLogAuditDetailDto dto = AddLogAuditDetailDto.builder()
            .userId(1L)
            .logAction(LogActions.LOG_IN)
            .jsonRequest("none").build();

        // Act
        Assertions.assertDoesNotThrow(() -> logAuditDetailService.writeLogAuditDetail(dto));
    }


}
