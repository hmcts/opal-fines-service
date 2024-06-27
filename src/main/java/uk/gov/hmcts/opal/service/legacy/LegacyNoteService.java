package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.authorisation.aspect.AuthorizedRoleHasPermission;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.legacy.LegacySaveNoteRequestDto;
import uk.gov.hmcts.opal.dto.legacy.LegacySaveNoteResponseDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.service.NoteServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.authorisation.model.Permissions.ACCOUNT_ENQUIRY_NOTES;

@Service
@Slf4j(topic = "LegacyNoteService")
public class LegacyNoteService extends LegacyService implements NoteServiceInterface {

    public static final String POST_ACCOUNT_NOTES = "postAccountNotes";

    protected LegacyNoteService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }


    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    @AuthorizedRoleHasPermission(ACCOUNT_ENQUIRY_NOTES)
    public NoteDto saveNote(NoteDto noteDto) {
        log.info("Saving Note: {}", noteDto);

        return postToGateway(POST_ACCOUNT_NOTES,
                    LegacySaveNoteResponseDto.class, LegacySaveNoteRequestDto.fromNoteDto(noteDto))
            .createClonedAndUpdatedDto(noteDto);
    }
    //TODO: change this to return entity rather than dto ?
    @Override
    @SuppressWarnings("unchecked")
    public List<NoteDto> searchNotes(NoteSearchDto searchCriteria) {
        log.info("Searching Notes: {}", searchCriteria);
        return postToGateway("searchNotes", List.class, searchCriteria);
    }


}
