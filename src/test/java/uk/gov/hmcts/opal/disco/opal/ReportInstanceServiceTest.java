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
import uk.gov.hmcts.opal.dto.search.ReportInstanceSearchDto;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportInstanceServiceTest {

    @Mock
    private ReportInstanceRepository reportInstanceRepository;

    @InjectMocks
    private ReportInstanceService reportInstanceService;

    @Test
    void testGetReportInstance() {
        // Arrange

        ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder().build();
        when(reportInstanceRepository.getReferenceById(any())).thenReturn(reportInstanceEntity);

        // Act
        ReportInstanceEntity result = reportInstanceService.getReportInstance(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchReportInstances() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder().build();
        Page<ReportInstanceEntity> mockPage = new PageImpl<>(List.of(reportInstanceEntity), Pageable.unpaged(), 999L);
        when(reportInstanceRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<ReportInstanceEntity> result = reportInstanceService.searchReportInstances(ReportInstanceSearchDto
                                                                                            .builder().build());

        // Assert
        assertEquals(List.of(reportInstanceEntity), result);

    }


}
