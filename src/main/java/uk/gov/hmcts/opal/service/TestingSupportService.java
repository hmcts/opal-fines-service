package uk.gov.hmcts.opal.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateFileItemTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateItemTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateRequestTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponseItemTestingSupport;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponseTestingSupport;
import uk.gov.hmcts.opal.mapper.testing.TestingSupportInterfaceJobMapper;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

@Service
@Slf4j(topic = "opal.TestingSupportService")
@RequiredArgsConstructor
public class TestingSupportService {

    private final InterfaceJobRepository interfaceJobRepository;
    private final BusinessUnitService businessUnitService;
    private final TestingSupportInterfaceJobMapper interfaceJobMapper;

    @Transactional
    public InterfaceJobsCreateResponseTestingSupport createInterfaceJobs(
        InterfaceJobsCreateRequestTestingSupport request) {

        List<InterfaceJobsCreateResponseItemTestingSupport> interfaceJobs = request.getInterfaceJobs()
            .stream()
            .map(this::createInterfaceJob)
            .map(interfaceJobMapper::toCreateResponseItem)
            .toList();

        return InterfaceJobsCreateResponseTestingSupport.builder()
            .interfaceJobs(interfaceJobs)
            .build();
    }

    private InterfaceJobEntity createInterfaceJob(InterfaceJobsCreateItemTestingSupport createJobItem) {
        InterfaceJobsCreateFileItemTestingSupport createFileItem = createJobItem.getInterfaceFile();
        InterfaceFileEntity interfaceFile = InterfaceFileEntity.builder()
            .fileName(createFileItem.getFileName())
            .source(createFileItem.getSource().getValue()) // TODO ask about OTHER vs new known values
            .overrideInhibits(createFileItem.getOverrideInhibits())
            .recordCount(createFileItem.getRecordCount())
            .totalAmount(createFileItem.getTotalAmount())
            .createdDateTime(createJobItem.getCreatedDatetime())
            .sourceJsonId(createFileItem.getSourceJsonId())
            .transformedJsonId(createFileItem.getTransformedJsonId())
            .build();

        BusinessUnitEntity businessUnit = businessUnitService.getBusinessUnit(createJobItem.getBusinessUnitId());
        InterfaceJobStatus status = InterfaceJobStatus.valueOf(createJobItem.getStatus().getValue());
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .businessUnit(businessUnit)
            .interfaceName(createJobItem.getInterfaceName())
            .status(status)
            .interfaceFiles(List.of(interfaceFile))
            .createdDateTime(createJobItem.getCreatedDatetime())
            .startedDateTime(createJobItem.getStartedDatetime())
            .completedDateTime(createJobItem.getCompletedDatetime())
            .build();

        return interfaceJobRepository.save(interfaceJob);
    }
}
