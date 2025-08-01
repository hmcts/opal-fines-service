package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.aspect.AuthorizedBusinessUnitUserHasPermission;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.NoteServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyNoteService;
import uk.gov.hmcts.opal.disco.opal.NoteService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

import static uk.gov.hmcts.opal.authorisation.model.Permissions.ACCOUNT_ENQUIRY_NOTES;

@Service
@RequiredArgsConstructor
@Qualifier("noteServiceProxy")
public class NoteServiceProxy implements NoteServiceInterface, ProxyInterface {

    private final NoteService opalNoteService;
    private final LegacyNoteService legacyNoteService;
    private final DynamicConfigService dynamicConfigService;

    private NoteServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyNoteService : opalNoteService;
    }

    @Override
    @FeatureToggle(feature = "add-note", value = true)
    @AuthorizedBusinessUnitUserHasPermission(ACCOUNT_ENQUIRY_NOTES)
    public NoteDto saveNote(NoteDto noteDto) {
        return getCurrentModeService().saveNote(noteDto);
    }

    @Override
    public List<NoteDto> searchNotes(NoteSearchDto searchCriteria) {
        return getCurrentModeService().searchNotes(searchCriteria);
    }
}
