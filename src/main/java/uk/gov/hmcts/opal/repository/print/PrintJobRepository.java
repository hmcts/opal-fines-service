package uk.gov.hmcts.opal.repository.print;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.opal.entity.print.PrintJob;


public interface PrintJobRepository extends JpaRepository<PrintJob, Long> {
}
