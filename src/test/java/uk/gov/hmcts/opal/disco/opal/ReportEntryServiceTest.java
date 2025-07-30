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
import uk.gov.hmcts.opal.dto.search.ReportEntrySearchDto;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.repository.ReportEntryRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportEntryServiceTest {

    @Mock
    private ReportEntryRepository reportEntryRepository;

    @InjectMocks
    private ReportEntryService reportEntryService;

    @Test
    void testGetReportEntry() {
        // Arrange

        ReportEntryEntity reportEntryEntity = ReportEntryEntity.builder().build();
        when(reportEntryRepository.getReferenceById(any())).thenReturn(reportEntryEntity);

        // Act
        ReportEntryEntity result = reportEntryService.getReportEntry(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchReportEntries() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        ReportEntryEntity reportEntryEntity = ReportEntryEntity.builder().build();
        Page<ReportEntryEntity> mockPage = new PageImpl<>(List.of(reportEntryEntity), Pageable.unpaged(), 999L);
        when(reportEntryRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<ReportEntryEntity> result = reportEntryService.searchReportEntries(ReportEntrySearchDto.builder().build());

        // Assert
        assertEquals(List.of(reportEntryEntity), result);

    }


}
