package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.annotation.JsonSchemaValidated;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.disco.DiscoDefendantAccountServiceInterface;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AddNoteDto;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.disco.opal.NoteService;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;
import static uk.gov.hmcts.opal.util.PermissionUtil.getRequiredBusinessUnitUser;

@RestController
@RequestMapping("/defendant-accounts")
@Slf4j(topic = "opal.DefendantAccountController")
@Tag(name = "Defendant Account Controller")
public class DefendantAccountController {

    public static final String NOTE_ASSOC_REC_TYPE = "defendant_accounts";

    private final DefendantAccountService defendantAccountService;

    // TODO - remove from controller
    private final DiscoDefendantAccountServiceInterface discoDefendantAccountServiceInterface;

    // TODO - remove from controller (probably)
    private final NoteService discoNoteService;

    // TODO - remove from controller
    private final UserStateService userStateService;

    public DefendantAccountController(DefendantAccountService defendantAccountService,
                                      @Qualifier("defendantAccountServiceProxy")
                                      DiscoDefendantAccountServiceInterface discoDefendantAccountServiceInterface,
                                      NoteService opalNoteService, UserStateService userStateService) {

        this.defendantAccountService = defendantAccountService;
        this.discoDefendantAccountServiceInterface = discoDefendantAccountServiceInterface;
        this.discoNoteService = opalNoteService;
        this.userStateService = userStateService;
    }

    @GetMapping
    @Operation(summary = "Searches for a defendant account in the Opal DB")
    public ResponseEntity<DefendantAccountEntity> getDefendantAccount(
        @RequestParam(name = "businessUnitId") Short businessUnitId,
        @RequestParam(name = "accountNumber") String accountNumber,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        AccountEnquiryDto enquiryDto = AccountEnquiryDto.builder()
            .businessUnitId(businessUnitId)
            .accountNumber(accountNumber)
            .build();

        DefendantAccountEntity response = discoDefendantAccountServiceInterface.getDefendantAccount(enquiryDto);

        return buildResponse(response);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates defendant account information")
    public ResponseEntity<DefendantAccountEntity> putDefendantAccount(
        @RequestBody DefendantAccountEntity defendantAccountEntity,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        DefendantAccountEntity response =
            discoDefendantAccountServiceInterface.putDefendantAccount(defendantAccountEntity);

        return buildResponse(response);
    }

    @GetMapping(value = "/{defendantAccountId}")
    @Operation(summary = "Get defendant account details by providing the defendant account summary")
    public ResponseEntity<AccountDetailsDto> getAccountDetails(@PathVariable Long defendantAccountId,
                     @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        AccountDetailsDto response =
            discoDefendantAccountServiceInterface.getAccountDetailsByDefendantAccountId(defendantAccountId);

        return buildResponse(response);
    }

    @PostMapping(value = "/addNote", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Adds a single note associated with the defendant account")
    public ResponseEntity<NoteDto> addNote(
        @RequestBody AddNoteDto addNote,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {
        log.debug(":POST:addNote: {}", addNote.toPrettyJson());

        UserState userState = userStateService.getUserStateUsingAuthToken(authHeaderValue);
        BusinessUnitUser businessUnitUser = getRequiredBusinessUnitUser(userState, addNote.getBusinessUnitId());

        NoteDto noteDto = NoteDto.builder()
            .associatedRecordId(addNote.getAssociatedRecordId())
            .noteText(addNote.getNoteText())
            .associatedRecordType(NOTE_ASSOC_REC_TYPE)
            .noteType("AA") // TODO - This will probably need to part of the AddNoteDto in future
            .businessUnitId(addNote.getBusinessUnitId())
            .businessUnitUserId(businessUnitUser.getBusinessUnitUserId())
            .postedByUsername(userState.getUserName())
            .postedDate(LocalDateTime.now())
            .build();

        NoteDto response = discoNoteService.saveNote(noteDto);

        log.debug(":POST:addNote: response: {}", response);

        return buildCreatedResponse(response);

    }

    @GetMapping(value = "/notes/{defendantId}")
    @Operation(summary = "Returns all notes for an associated defendant account id.")
    public ResponseEntity<List<NoteDto>> getNotesForDefendantAccount(
        @PathVariable String defendantId,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":GET:getNotesForDefendantAccount: defendant account id: {}", defendantId);

        NoteSearchDto criteria = NoteSearchDto.builder()
            .associatedType(NOTE_ASSOC_REC_TYPE)
            .associatedId(defendantId)
            .build();

        List<NoteDto> response = discoNoteService.searchNotes(criteria);

        return buildResponse(response);
    }

    @GetMapping(value = "/{defendantAccountId}/header-summary")
    @Operation(summary = "Get defendant account details by providing the defendant account summary")
    public ResponseEntity<DefendantAccountHeaderSummary> getHeaderSummary(@PathVariable Long defendantAccountId,
              @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":GET:getHeaderSummary: for defendant id: {}", defendantAccountId);

        return buildResponse(defendantAccountService.getHeaderSummary(defendantAccountId, authHeaderValue));
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches defendant accounts based upon criteria in request body")
    public ResponseEntity<DefendantAccountSearchResultsDto> postDefendantAccountSearch(
        @JsonSchemaValidated(schemaPath = SchemaPaths.POST_DEFENDANT_ACCOUNT_SEARCH_REQUEST)
        @RequestBody AccountSearchDto accountSearchDto,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {
        log.debug(":POST:postDefendantAccountSearch: query: \n{}", accountSearchDto.toPrettyJson());

        DefendantAccountSearchResultsDto response =
            defendantAccountService.searchDefendantAccounts(accountSearchDto, authHeaderValue);

        return buildResponse(response);
    }
}
