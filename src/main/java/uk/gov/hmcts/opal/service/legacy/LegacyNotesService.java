package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyNote;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyNotesService")
public class LegacyNotesService implements NotesServiceInterface {

    private final GatewayService gatewayService;

    private static final String ADD_NOTE = "LIBRA.add_note";


    @Override
    public String addNote(AddNoteRequest request, Long version, UserState user) {
        log.info(":LegacyAddNote");

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> response =
            gatewayService.postToGateway(ADD_NOTE,
                                         LegacyMinorCreditorSearchResultsResponse.class,
                                         createRequest(request, version, user), null
            );

        return null;
    }

    private LegacyAddNoteRequest createRequest(AddNoteRequest request, Long version, UserState user) {

    LegacyNote note = LegacyNote.builder()
        .noteText(request.getActivityNote().getNoteText())
        .noteType(request.getActivityNote().getNoteType())
        .recordType(request.getActivityNote().getRecordType())
        .recordId(request.getActivityNote().getRecordId()).build();

            return LegacyAddNoteRequest.builder().version(version.intValue()).activityNote(note).build();
    }

}
