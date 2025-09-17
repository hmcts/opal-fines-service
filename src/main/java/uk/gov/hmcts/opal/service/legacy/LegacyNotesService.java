package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyNotesService")
public class LegacyNotesService implements NotesServiceInterface {

    @Override
    public String addNote(AddNoteRequest request, Long version) {
        log.info(":LegacyAddNote is not being implemented yet.");

        return null;
    }
}
