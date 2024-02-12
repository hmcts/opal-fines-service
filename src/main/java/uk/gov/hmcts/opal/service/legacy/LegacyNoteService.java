package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.dto.legacy.LegacySaveNoteRequestDto;
import uk.gov.hmcts.opal.dto.legacy.LegacySaveNoteResponseDto;
import uk.gov.hmcts.opal.service.NoteServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyNoteService")
public class LegacyNoteService extends LegacyService implements NoteServiceInterface {

    public static final String POST_ACCOUNT_NOTES = "postAccountNotes";

    protected LegacyNoteService(@Value("${legacy-gateway.url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    public NoteDto saveNote(NoteDto noteDto) {
        log.info("Saving Note: {}", noteDto);

        return postToGateway(POST_ACCOUNT_NOTES,
                    LegacySaveNoteResponseDto.class, LegacySaveNoteRequestDto.fromNoteDto(noteDto))
            .createClonedAndUpdatedDto(noteDto);
    }

    @Override
    public List<NoteDto> searchNotes(NoteSearchDto searchCriteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }


}
