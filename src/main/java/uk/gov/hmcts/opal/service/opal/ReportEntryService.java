package uk.gov.hmcts.opal.service.opal;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    public static final String LIST_EXTEND_TTP = "list_extend_ttp";
    public static final String PAYMENT_TERMS = "payment_terms";

    @Autowired
    private ReportEntryRepository reportEntryRepository;

    @Autowired
    private BusinessUnitService businessUnitService;

    /**
     * Create a report_entries record for the Extension of Time to Pay report.
     *
     */
    public void createExtendTtpReportEntry(Long paymentTermsId, short businessUnitId) {
        ReportEntryEntity reportEntry = new ReportEntryEntity();

        reportEntry.setBusinessUnit(businessUnitService.getBusinessUnit(businessUnitId));
        reportEntry.setReportId(LIST_EXTEND_TTP);
        reportEntry.setEntryTimestamp(LocalDateTime.now());
        reportEntry.setAssociatedRecordId(String.valueOf(paymentTermsId));
        reportEntry.setAssociatedRecordType(PAYMENT_TERMS);

        reportEntryRepository.save(reportEntry);

        log.debug(":createExtendTtpReportEntry: created report entry for paymentTermsId={} BU={}",
            paymentTermsId, businessUnitId);
    }
}
