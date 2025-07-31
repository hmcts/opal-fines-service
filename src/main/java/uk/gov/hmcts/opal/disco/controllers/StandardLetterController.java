package uk.gov.hmcts.opal.disco.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.search.StandardLetterSearchDto;
import uk.gov.hmcts.opal.entity.StandardLetterEntity;
import uk.gov.hmcts.opal.disco.StandardLetterServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/standard-letters")
@Slf4j(topic = "StandardLetterController")
@Tag(name = "StandardLetter Controller")
public class StandardLetterController {

    private final StandardLetterServiceInterface standardLetterService;

    public StandardLetterController(@Qualifier("standardLetterService")
                                    StandardLetterServiceInterface standardLetterService) {
        this.standardLetterService = standardLetterService;
    }

    @GetMapping(value = "/{standardLetterId}")
    @Operation(summary = "Returns the StandardLetter for the given standardLetterId.")
    public ResponseEntity<StandardLetterEntity> getStandardLetterById(@PathVariable Long standardLetterId) {

        log.debug(":GET:getStandardLetterById: standardLetterId: {}", standardLetterId);

        StandardLetterEntity response = standardLetterService.getStandardLetter(standardLetterId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Standard Letters based upon criteria in request body")
    public ResponseEntity<List<StandardLetterEntity>> postStandardLettersSearch(@RequestBody
                                                                                    StandardLetterSearchDto criteria) {
        log.debug(":POST:postStandardLettersSearch: query: \n{}", criteria);

        List<StandardLetterEntity> response = standardLetterService.searchStandardLetters(criteria);

        return buildResponse(response);
    }


}
