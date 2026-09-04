package uk.gov.hmcts.opal.service.legacy;

import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyNote;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.service.AccountNoteContext;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyNotesService")
public class LegacyNotesService implements NotesServiceInterface {

    private static final String ADD_NOTE = "addNote";
    private final GatewayService gatewayService;

    @Override
    public String addNote(AddNoteRequestNotes request, String ifMatch, UserState user, AccountNoteContext target) {
        log.info(":LegacyAddNote");

        GatewayService.Response<LegacyAddNoteResponse> response = gatewayService.postToGateway(
            ADD_NOTE,
            LegacyAddNoteResponse.class,
            createRequest(request, ifMatch, user, target.businessUnitId()),
            null
        );

        if (response.isError()) {
            log.error(":LegacyAddNote: Legacy Gateway response: HTTP Response Code: {}", response.code);

            if (response.isException()) {
                log.error(":LegacyAddNote:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":LegacyAddNote: Legacy Gateway: body: \n{}", response.body);
                LegacyAddNoteResponse responseEntity = response.responseEntity;
                log.error(":LegacyAddNote: Legacy Gateway: entity: \n{}", responseEntity.toXml());
            }
        } else if (response.isSuccessful()) {
            log.info(":LegacyAddNote: Legacy Gateway response: Success.");
        }

        return response.responseEntity.getNote().getRecordId();
    }

    private LegacyAddNoteRequest createRequest(AddNoteRequestNotes request, String version, UserState user,
                                               Short businessUnitId) {

        LegacyNote note = LegacyNote.builder().noteText(request.getActivityNote().getNoteText())
            .noteType(request.getActivityNote().getNoteType().getValue())
            .recordType(RecordType.valueOf(request.getActivityNote().getRecordType().name()))
            .recordId(request.getActivityNote().getRecordId()).build();

        return LegacyAddNoteRequest.builder().businessUnitId(businessUnitId)
            .businessUnitUserId(user.getUserId()).version(new BigInteger(version)).activityNote(note).build();
    }
}
