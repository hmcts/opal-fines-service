package uk.gov.hmcts.opal.controllers.develop;

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
import uk.gov.hmcts.opal.dto.search.ResultDocumentSearchDto;
import uk.gov.hmcts.opal.entity.ResultDocumentEntity;
import uk.gov.hmcts.opal.service.ResultDocumentServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/result-documents")
@Slf4j(topic = "ResultDocumentController")
@Tag(name = "ResultDocument Controller")
public class ResultDocumentController {

    private final ResultDocumentServiceInterface resultDocumentService;

    public ResultDocumentController(@Qualifier("resultDocumentService")
                                    ResultDocumentServiceInterface resultDocumentService) {
        this.resultDocumentService = resultDocumentService;
    }

    @GetMapping(value = "/{resultDocumentId}")
    @Operation(summary = "Returns the ResultDocument for the given resultDocumentId.")
    public ResponseEntity<ResultDocumentEntity> getResultDocumentById(@PathVariable Long resultDocumentId) {

        log.debug(":GET:getResultDocumentById: resultDocumentId: {}", resultDocumentId);

        ResultDocumentEntity response = resultDocumentService.getResultDocument(resultDocumentId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Result Documents based upon criteria in request body")
    public ResponseEntity<List<ResultDocumentEntity>> postResultDocumentsSearch(@RequestBody
                                                                                    ResultDocumentSearchDto criteria) {
        log.debug(":POST:postResultDocumentsSearch: query: \n{}", criteria);

        List<ResultDocumentEntity> response = resultDocumentService.searchResultDocuments(criteria);

        return buildResponse(response);
    }


}
