package uk.gov.hmcts.opal.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.opal.dto.NoteDto;

@Service
@Slf4j
public class LegacyNoteService extends LegacyService implements NoteServiceInterface {

    public static final String POST_ACCOUNT_NOTES = "postAccountNotes";

    protected LegacyNoteService(@Value("${legacy-gateway-url}") String gatewayUrl, RestTemplate restTemplate) {
        super(gatewayUrl, restTemplate);
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    @Override
    public NoteDto saveNote(NoteDto noteDto) {
        log.info("Saving Note: {}", noteDto);
        return postToGateway(POST_ACCOUNT_NOTES, NoteDto.class, noteDto);
    }


}
