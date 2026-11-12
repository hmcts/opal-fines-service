package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateFileItemTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateItemTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateRequestTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponseTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsFileSourceTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsJobStatusTestingSupport;
import uk.gov.hmcts.opal.mapper.testing.TestingSupportInterfaceJobMapper;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

@ExtendWith(MockitoExtension.class)
public class TestingSupportServiceTest {
    @Mock
    private TestingSupportInterfaceJobMapper interfaceJobMapper;

    @Mock
    private InterfaceJobRepository interfaceJobRepository;

    @Mock
    private BusinessUnitService businessUnitService;

    @InjectMocks
    private TestingSupportService service;

    @Test
    void createInterfaceJobs_shouldCorrectlyCreateJobsFromRequest() {
        // Arrange
        final String fileName = "some-file.dat";
        final short recordCount = 5;
        final long sourceJsonId = 800L;
        final long transformedJsonId = 900L;
        final BigDecimal totalAmount = BigDecimal.valueOf(150L);
        final short businessUnitId = 30;
        final String interfaceName = "Inter Face Name";
        final LocalDateTime created = LocalDateTime.of(2026, Month.APRIL, 1, 12, 0);
        final LocalDateTime started = LocalDateTime.of(2026, Month.APRIL, 2, 10, 0);
        final LocalDateTime completed = LocalDateTime.of(2026, Month.APRIL, 3, 9, 0);
        BusinessUnitEntity businessUnit = mock(BusinessUnitEntity.class);
        when(businessUnitService.getBusinessUnit(businessUnitId)).thenReturn(businessUnit);
        when(interfaceJobRepository.save(any(InterfaceJobEntity.class))).then(AdditionalAnswers.returnsFirstArg());

        InterfaceJobsCreateFileItemTestingSupport fileItem = InterfaceJobsCreateFileItemTestingSupport.builder()
            .fileName(fileName)
            .overrideInhibits(true)
            .recordCount(recordCount)
            .source(InterfaceJobsFileSourceTestingSupport.CDER)
            .totalAmount(totalAmount)
            .sourceJsonId(sourceJsonId)
            .transformedJsonId(transformedJsonId)
            .build();

        InterfaceJobsCreateItemTestingSupport jobItem = InterfaceJobsCreateItemTestingSupport.builder()
            .businessUnitId(businessUnitId)
            .interfaceFile(fileItem)
            .interfaceName(interfaceName)
            .status(InterfaceJobsJobStatusTestingSupport.COMPLETED)
            .createdDatetime(created)
            .startedDatetime(started)
            .completedDatetime(completed)
            .build();

        InterfaceJobsCreateRequestTestingSupport request = new InterfaceJobsCreateRequestTestingSupport();
        request.addInterfaceJobsItem(jobItem);

        // Act
        InterfaceJobsCreateResponseTestingSupport response = service.createInterfaceJobs(request);

        // Assert
        ArgumentCaptor<InterfaceJobEntity> jobEntityArgumentCaptor = ArgumentCaptor.forClass(InterfaceJobEntity.class);
        verify(interfaceJobRepository, times(1)).save(jobEntityArgumentCaptor.capture());
        InterfaceJobEntity createdJob = jobEntityArgumentCaptor.getValue();

        assertThat(createdJob.getBusinessUnit()).isSameAs(businessUnit);
        assertThat(createdJob.getInterfaceName()).isEqualTo(interfaceName);
        assertThat(createdJob.getStatus()).isEqualTo(InterfaceJobStatus.COMPLETED);
        assertThat(createdJob.getCreatedDateTime()).isEqualTo(created);
        assertThat(createdJob.getStartedDateTime()).isEqualTo(started);
        assertThat(createdJob.getCompletedDateTime()).isEqualTo(completed);

        assertThat(createdJob.getInterfaceFiles()).singleElement();
        InterfaceFileEntity interfaceFile = createdJob.getInterfaceFiles().getFirst();
        assertThat(interfaceFile.getFileName()).isEqualTo(fileName);
        assertThat(interfaceFile.getRecordCount()).isEqualTo(recordCount);
        assertThat(interfaceFile.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(interfaceFile.getSource()).isEqualTo("CDER");
        assertThat(interfaceFile.getSourceJsonId()).isEqualTo(sourceJsonId);
        assertThat(interfaceFile.getTransformedJsonId()).isEqualTo(transformedJsonId);
        assertThat(interfaceFile.getCreatedDateTime()).isEqualTo(created);

        verify(interfaceJobMapper, times(1)).toCreateResponseItem(createdJob);
    }
}
