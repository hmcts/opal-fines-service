package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.AddNoteDto;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.NoteServiceInterface;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

@RestController
@RequestMapping("/api/defendant-account")
@Slf4j(topic = "DefendantAccountController")
@Tag(name = "Defendant Account Controller")
public class DefendantAccountController {

    public static final String NOTE_ASSOC_REC_TYPE = "defendant_accounts";

    private final DefendantAccountServiceInterface defendantAccountService;

    private final NoteServiceInterface noteService;

    private final UserStateService userStateService;

    public DefendantAccountController(
        @Qualifier("defendantAccountServiceProxy") DefendantAccountServiceInterface defendantAccountService,
        @Qualifier("noteServiceProxy") NoteServiceInterface noteService, UserStateService userStateService) {

        this.defendantAccountService = defendantAccountService;
        this.noteService = noteService;
        this.userStateService = userStateService;
    }

    @GetMapping
    @Operation(summary = "Searches for a defendant account in the Opal DB")
    public ResponseEntity<DefendantAccountEntity> getDefendantAccount(
        @RequestParam(name = "businessUnitId") Short businessUnitId,
        @RequestParam(name = "accountNumber") String accountNumber) {

        AccountEnquiryDto request = AccountEnquiryDto.builder()
            .businessUnitId(businessUnitId)
            .accountNumber(accountNumber)
            .build();

        DefendantAccountEntity response = defendantAccountService.getDefendantAccount(request);

        return buildResponse(response);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates defendant account information")
    public ResponseEntity<DefendantAccountEntity> putDefendantAccount(
        @RequestBody DefendantAccountEntity defendantAccountEntity) {

        DefendantAccountEntity response = defendantAccountService.putDefendantAccount(defendantAccountEntity);

        return buildResponse(response);
    }

    @GetMapping(value = "/{defendantAccountId}")
    @Operation(summary = "Get defendant account details by providing the defendant account summary")
    public ResponseEntity<AccountDetailsDto> getAccountDetails(@PathVariable Long defendantAccountId) {

        AccountDetailsDto response = defendantAccountService.getAccountDetailsByDefendantAccountId(defendantAccountId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches defendant accounts based upon criteria in request body")
    public ResponseEntity<AccountSearchResultsDto> postDefendantAccountSearch(
        @RequestBody AccountSearchDto accountSearchDto) {
        log.info(":POST:postDefendantAccountSearch: query: \n{}", accountSearchDto.toPrettyJson());

        AccountSearchResultsDto response = defendantAccountService.searchDefendantAccounts(accountSearchDto);

        return buildResponse(response);
    }

    @PostMapping(value = "/addNote", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Adds a single note associated with the defendant account")
    public ResponseEntity<NoteDto> addNote(
        @RequestBody AddNoteDto addNote, HttpServletRequest request) {
        log.info(":POST:addNote: {}", addNote.toPrettyJson());

        UserState userState = userStateService.getUserStateUsingServletRequest(request);

        NoteDto noteDto = NoteDto.builder()
            .associatedRecordId(addNote.getAssociatedRecordId())
            .noteText(addNote.getNoteText())
            .associatedRecordType(NOTE_ASSOC_REC_TYPE)
            .noteType("AA") // TODO - This will probably need to part of the AddNoteDto in future
            .postedBy(userState.getFirstRoleBusinessUserId().orElse(userState.getUserName())) // TODO - always first?
            .postedByAAD(userState.getUserId())
            .postedDate(LocalDateTime.now())
            .build();

        NoteDto response = noteService.saveNote(noteDto);

        log.info(":POST:addNote: response: {}", response);

        return buildCreatedResponse(response);

    }

    @GetMapping(value = "/notes/{defendantId}")
    @Operation(summary = "Returns all notes for an associated defendant account id.")
    public ResponseEntity<List<NoteDto>> getNotesForDefendantAccount(@PathVariable String defendantId) {

        log.info(":GET:getNotesForDefendantAccount: defendant account id: {}", defendantId);

        NoteSearchDto criteria = NoteSearchDto.builder()
            .associatedType(NOTE_ASSOC_REC_TYPE)
            .associatedId(defendantId)
            .build();

        List<NoteDto> response = noteService.searchNotes(criteria);

        return buildResponse(response);
    }

}
