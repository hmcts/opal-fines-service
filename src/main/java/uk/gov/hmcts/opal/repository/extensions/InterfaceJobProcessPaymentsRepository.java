package uk.gov.hmcts.opal.repository.extensions;

import org.springframework.stereotype.Repository;

@Repository
public interface InterfaceJobProcessPaymentsRepository {
    Long processPaymentsInJob(Long interfaceJobId,
                              Short businessUnitId,
                              String postedBy,
                              String postedByName,
                              String recordsJson);
}
