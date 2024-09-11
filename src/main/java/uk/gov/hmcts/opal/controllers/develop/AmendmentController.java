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
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AmendmentEntity;
import uk.gov.hmcts.opal.service.AmendmentServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/amendments")
@Slf4j(topic = "AmendmentController")
@Tag(name = "Amendment Controller")
public class AmendmentController {

    private final AmendmentServiceInterface amendmentService;

    public AmendmentController(@Qualifier("amendmentServiceProxy") AmendmentServiceInterface amendmentService) {
        this.amendmentService = amendmentService;
    }

    @GetMapping(value = "/{amendmentId}")
    @Operation(summary = "Returns the Amendment for the given amendmentId.")
    public ResponseEntity<AmendmentEntity> getAmendmentById(@PathVariable Long amendmentId) {

        log.info(":GET:getAmendmentById: amendmentId: {}", amendmentId);

        AmendmentEntity response = amendmentService.getAmendment(amendmentId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Amendments based upon criteria in request body")
    public ResponseEntity<List<AmendmentEntity>> postAmendmentsSearch(@RequestBody AmendmentSearchDto criteria) {
        log.info(":POST:postAmendmentsSearch: query: \n{}", criteria);

        List<AmendmentEntity> response = amendmentService.searchAmendments(criteria);

        return buildResponse(response);
    }


}
