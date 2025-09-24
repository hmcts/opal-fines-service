package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyNote;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyNotesService")
public class LegacyNotesService implements NotesServiceInterface {

    private static final String ADD_NOTE = "LIBRA.add_note";
    private final GatewayService gatewayService;

    @Override
    public String addNote(AddNoteRequest request, Long version, UserState user, DefendantAccountEntity account) {
        log.info(":LegacyAddNote");

        GatewayService.Response<LegacyAddNoteResponse> response =
            gatewayService.postToGateway(
                ADD_NOTE, LegacyAddNoteResponse.class,
                createRequest(request, version, user, account.getBusinessUnit().getBusinessUnitId().toString()), null);

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

    private LegacyAddNoteRequest createRequest(AddNoteRequest request, Long version,
                                               UserState user, String defendantBusinessUnitId) {

        LegacyNote note =
            LegacyNote.builder()
                .noteText(request.getActivityNote().getNoteText())
                .noteType(request.getActivityNote().getNoteType())
                .recordType(request.getActivityNote().getRecordType())
                .recordId(request.getActivityNote().getRecordId())
                .build();

        return LegacyAddNoteRequest.builder()
            .businessUnitId(defendantBusinessUnitId)
            .businessUnitUserId(user.getUserId().toString())
            .version(version)
            .activityNote(note)
            .build();
    }
}
