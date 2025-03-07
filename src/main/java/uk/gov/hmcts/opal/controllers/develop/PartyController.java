package uk.gov.hmcts.opal.controllers.develop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.service.PartyServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/parties")
@Slf4j(topic = "PartyController")
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

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches parties based upon criteria in request body")
    public ResponseEntity<List<PartyEntity>> postPartiesSearch(@RequestBody PartySearchDto criteria) {
        log.debug(":POST:postPartiesSearch: query: \n{}", criteria);

        List<PartyEntity> response = partyService.searchParties(criteria);

        return buildResponse(response);
    }







}
