package uk.gov.hmcts.opal.service.legacy;

import static uk.gov.hmcts.opal.util.VersionUtils.extractBigInteger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyNote;
import uk.gov.hmcts.opal.service.AccountNoteContext;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyNotesService")
public class LegacyNotesService implements NotesServiceInterface {

    private static final String ADD_NOTE = "addNote";
    private final GatewayService gatewayService;

    @Override
    public String addNote(AddNoteRequest request, String ifMatch, UserState user, AccountNoteContext target) {
        return addNote(request, ifMatch, user, target.businessUnitId());
    }

    public String addNote(AddNoteRequest request, String ifMatch, UserState user, Short businessUnitId) {
        log.info(":LegacyAddNote");

        GatewayService.Response<LegacyAddNoteResponse> response = gatewayService.postToGateway(
            ADD_NOTE,
            LegacyAddNoteResponse.class,
            createRequest(request, ifMatch, user, businessUnitId),
            null
        );

        if (response.isError()) {
            handleGatewayError(response);
        } else if (response.isSuccessful()) {
            log.info(":LegacyAddNote: Legacy Gateway response: Success.");
        }

        if (response.responseEntity != null && response.responseEntity.getErrorResponse() != null) {
            log.error(":LegacyAddNote: Legacy Gateway error response: {}", response.responseEntity.getErrorResponse());
            throw new IllegalArgumentException("Legacy gateway returned failure");
        }

        if (response.responseEntity == null || response.responseEntity.getNote() == null) {
            throw new IllegalArgumentException("Legacy add note response missing activity note");
        }

        return response.responseEntity.getNote().getRecordId();
    }

    private LegacyAddNoteRequest createRequest(AddNoteRequest request, String version, UserState user,
                                               Short businessUnitId) {

        LegacyNote note = LegacyNote.builder().noteText(request.getActivityNote().getNoteText())
            .noteType(request.getActivityNote().getNoteType()).recordType(request.getActivityNote().getRecordType())
            .recordId(request.getActivityNote().getRecordId()).build();

        return LegacyAddNoteRequest.builder().businessUnitId(businessUnitId)
            .businessUnitUserId(getBusinessUnitUserId(user, businessUnitId))
            .version(extractBigInteger(version)).activityNote(note).build();
    }

    private String getBusinessUnitUserId(UserState user, Short businessUnitId) {
        return user.getBusinessUnitUserForBusinessUnit(businessUnitId)
            .map(BusinessUnitUser::getBusinessUnitUserId)
            .filter(id -> !id.isBlank())
            .orElse(user.getUserName());
    }

    private void handleGatewayError(GatewayService.Response<LegacyAddNoteResponse> response) {
        log.error(":LegacyAddNote: Legacy Gateway response: HTTP Response Code: {}", response.code);

        if (response.isException()) {
            log.error(":LegacyAddNote:", response.exception);
            throw new IllegalArgumentException("Legacy gateway exception", response.exception);
        }

        if (response.isLegacyFailure()) {
            log.error(":LegacyAddNote: Legacy Gateway: body: \n{}", response.body);
            LegacyAddNoteResponse responseEntity = response.responseEntity;
            if (responseEntity != null) {
                log.error(":LegacyAddNote: Legacy Gateway: entity: \n{}", responseEntity.toXml());
            }
            throw new IllegalArgumentException("Legacy gateway returned failure");
        }

        throw new IllegalArgumentException("Legacy gateway error: " + response.code);
    }
}
