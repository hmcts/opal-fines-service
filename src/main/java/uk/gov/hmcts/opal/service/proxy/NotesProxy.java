package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyNotesService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalNotesService;

@Service
@Slf4j(topic = "opal.DefendantAccountServiceProxy")
@RequiredArgsConstructor
public class NotesProxy implements NotesServiceInterface, ProxyInterface {

    private final OpalNotesService notesService;
    private final LegacyNotesService legacyNotesService;
    private final DynamicConfigService dynamicConfigService;

    private NotesServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyNotesService : notesService;
    }

    @Override
    public String addNote(AddNoteRequest request, Long version) {
        return getCurrentModeService().addNote(request, version);
    }

}
