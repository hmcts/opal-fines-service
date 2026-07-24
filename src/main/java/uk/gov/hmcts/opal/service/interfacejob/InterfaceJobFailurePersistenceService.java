package uk.gov.hmcts.opal.service.interfacejob;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.InterfaceMessageRepository;

@Service
@RequiredArgsConstructor
public class InterfaceJobFailurePersistenceService {

    private static final String MESSAGE_TYPE_ERROR = "Error";

    private final InterfaceJobRepository interfaceJobRepository;
    private final InterfaceMessageRepository interfaceMessageRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertFailureMessage(Long interfaceJobId, Exception exception) {
        InterfaceJobEntity interfaceJob = interfaceJobRepository.findById(interfaceJobId)
            .orElseThrow(() -> new IllegalStateException("Interface job not found with id: " + interfaceJobId));
        Long interfaceFileId = interfaceJob.getInterfaceFiles().stream()
            .filter(Objects::nonNull)
            .map(InterfaceFileEntity::getInterfaceFileId)
            .findFirst()
            .orElse(null);
        interfaceMessageRepository.insertInterfaceMessage(
            interfaceJobId,
            MESSAGE_TYPE_ERROR,
            failureMessage(exception),
            interfaceFileId,
            null,
            null,
            null
        );
    }

    private String failureMessage(Exception exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        String message = rootCause.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getMessage();
        }
        if (message == null || message.isBlank()) {
            message = rootCause.getClass().getSimpleName();
        }
        message = message.replace('\n', ' ').replace('\r', ' ');
        message = stripSqlLocationDetails(message);
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private String stripSqlLocationDetails(String message) {
        int whereIndex = message.indexOf(" Where:");
        if (whereIndex < 0) {
            whereIndex = message.indexOf(" where:");
        }
        return whereIndex >= 0 ? message.substring(0, whereIndex) : message;
    }
}
