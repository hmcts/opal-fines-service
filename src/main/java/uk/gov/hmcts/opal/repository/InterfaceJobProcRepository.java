package uk.gov.hmcts.opal.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface InterfaceJobProcRepository {
    Long processPaymentsInJob(Long interfaceJobId,
                              Short businessUnitId,
                              String postedBy,
                              String postedByName,
                              String recordsJson);
}
