package uk.gov.hmcts.opal.repository.projection;

import java.time.LocalDateTime;

public interface MinorCreditorNoteHistoryProjection {

    Long getNoteId();

    LocalDateTime getPostedDate();

    String getPostedBy();

    String getPostedByName();

    String getNoteText();
}
