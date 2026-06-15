package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.util.RecordTypeConstants.PAYMENT_TERMS;
import static uk.gov.hmcts.opal.util.ReportIdConstants.LIST_EXTEND_TTP;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.repository.ReportEntryRepository;
import uk.gov.hmcts.opal.service.iface.ReportEntryServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.ReportEntryService")
@Qualifier("reportEntryService")
public class ReportEntryService implements ReportEntryServiceInterface {

    private final ReportEntryRepository reportEntryRepository;

    private final BusinessUnitService businessUnitService;

    private final Clock clock;

    /**
     * Create a report_entries record for the Extension of Time to Pay report.
     *
     */
    public void createExtendTtpReportEntry(Long paymentTermsId, short businessUnitId) {
        ReportEntryEntity reportEntry = ReportEntryEntity.builder()
            .businessUnit(businessUnitService.getBusinessUnit(businessUnitId))
            .reportId(LIST_EXTEND_TTP)
            .entryTimestamp(LocalDateTime.now(clock))
            .associatedRecordId(String.valueOf(paymentTermsId))
            .associatedRecordType(PAYMENT_TERMS)
            .build();

        reportEntryRepository.save(reportEntry);

        log.debug(":createExtendTtpReportEntry: created report entry for paymentTermsId={} BU={}",
            paymentTermsId, businessUnitId);
    }
}
