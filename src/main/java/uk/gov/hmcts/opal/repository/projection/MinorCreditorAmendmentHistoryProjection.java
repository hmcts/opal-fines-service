package uk.gov.hmcts.opal.repository.projection;

import java.time.LocalDateTime;

public interface MinorCreditorAmendmentHistoryProjection {

    Long getAmendmentId();

    LocalDateTime getPostedDate();

    String getPostedBy();

    String getPostedByName();

    String getAttributeName();

    String getOldValue();

    String getNewValue();
}
