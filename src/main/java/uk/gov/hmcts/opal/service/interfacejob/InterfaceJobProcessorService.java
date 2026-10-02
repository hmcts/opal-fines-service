package uk.gov.hmcts.opal.service.interfacejob;

import static java.lang.String.format;

import java.nio.charset.Charset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerInterfaceFiles;
import uk.gov.hmcts.opal.service.interfacejob.json.InterfaceJobJsonMapper;
import uk.gov.hmcts.opal.service.interfacejob.json.records.InterfaceJobRecord;

@Service
@RequiredArgsConstructor
public class InterfaceJobProcessorService {

    private static final String SYSTEM_POSTED_BY = "interface-jobs";
    private static final String SYSTEM_POSTED_BY_NAME = "interface-jobs";

    private final InterfaceJobRepository interfaceJobRepository;
    private final FileHandlerInterfaceFiles fileHandlerInterfaceFiles;
    private final InterfaceJobJsonMapper jsonMapper;

    @Transactional
    public Optional<Long> processPaymentsInJob(Long interfaceJobId) {
        try {
            InterfaceJobEntity interfaceJob = interfaceJobRepository.findById(interfaceJobId)
                .orElseThrow(() -> new IllegalStateException("Interface job not found with id: " + interfaceJobId));
            Long transformedJsonId = getTransformedJsonId(interfaceJob);
            Resource fileContent = fileHandlerInterfaceFiles
                .getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER, transformedJsonId);
            String json = fileContent.getContentAsString(Charset.defaultCharset());
            String recordsJson = jsonMapper.toRecordsJson(json);

            return Optional.ofNullable(interfaceJobRepository.processPaymentsInJob(
                interfaceJob.getInterfaceJobId(),
                interfaceJob.getBusinessUnit().getBusinessUnitId(),
                SYSTEM_POSTED_BY,
                SYSTEM_POSTED_BY_NAME,
                recordsJson));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to process interface job " + interfaceJobId, e);
        }
    }

    private Long getTransformedJsonId(InterfaceJobEntity interfaceJob) {
        Long transformedJsonId = interfaceJob.getInterfaceFiles()
            .getFirst() // Note - this should really be a one-to-one (the model might change)
            .getTransformedJsonId();
        if(transformedJsonId == null) {
            String errorMsg = format(
                "Interface job %s does not have associated transformedJsonId", interfaceJob.getInterfaceJobId());
            throw new IllegalStateException(errorMsg);
        }
        return transformedJsonId;
    }
}
