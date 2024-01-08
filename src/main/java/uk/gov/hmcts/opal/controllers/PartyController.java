package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.service.PartyServiceInterface;


@RestController
@RequestMapping("/api/party")
@Tag(name = "Party Controller")
public class PartyController {


    private final PartyServiceInterface partyService;

    public PartyController(@Qualifier("partyServiceProxy") PartyServiceInterface partyService) {
        this.partyService = partyService;
    }

    @GetMapping(value = "/{partyId}")
    @Operation(summary = "Returns a Party based upon the party id")
    public ResponseEntity<PartyDto> getParty(@PathVariable long partyId) {

        PartyDto response = partyService.getParty(partyId);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Creates a new party in the Opal Party table assigning an ID.")
    public ResponseEntity<PartyDto> createParty(@RequestBody PartyDto partyDto) {
        PartyDto savedPartyDto = partyService.saveParty(partyDto);
        return new ResponseEntity<>(savedPartyDto, HttpStatus.CREATED);
    }







}
