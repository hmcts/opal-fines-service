package uk.gov.hmcts.opal.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.projection.SuspenseTransactionHistoryProjection;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;
import uk.gov.hmcts.opal.generated.model.SuspenseNoteCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseTransactionCommonStrict;

@Component
public class SuspenseItemHistoryMapper {

    public SuspenseNoteCommonStrict toSuspenseNote(NoteEntity entity) {
        NoteCommon note = new NoteCommon()
            .recordType(NoteCommon.RecordTypeEnum.fromValue(
                entity.getAssociatedRecordType().getLabel()))
            .recordId(entity.getAssociatedRecordId())
            .noteText(entity.getNoteText())
            .noteType(NoteCommon.NoteTypeEnum.fromValue(
                entity.getNoteType().name()));

        PostedDetailsCommon postedDetails = new PostedDetailsCommon()
            .postedDate(entity.getPostedDate())
            .postedBy(entity.getBusinessUnitUserId())
            .postedByName(entity.getPostedByUsername());

        return new SuspenseNoteCommonStrict()
            .note(note)
            .postedDetails(postedDetails);
    }

    public SuspenseTransactionCommonStrict toSuspenseTransaction(SuspenseTransactionHistoryProjection row) {
        PostedDetailsCommon postedDetails = new PostedDetailsCommon()
            .postedDate(row.getPostedDate())
            .postedBy(row.getPostedBy())
            .postedByName(row.getPostedByName());

        return new SuspenseTransactionCommonStrict()
            .transactionId(row.getTransactionId())
            .postedDetails(postedDetails);
    }

}