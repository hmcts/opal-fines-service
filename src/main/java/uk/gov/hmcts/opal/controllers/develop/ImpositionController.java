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
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.ImpositionEntity;
import uk.gov.hmcts.opal.service.ImpositionServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/imposition")
@Slf4j(topic = "ImpositionController")
@Tag(name = "Imposition Controller")
public class ImpositionController {

    private final ImpositionServiceInterface impositionService;

    public ImpositionController(@Qualifier("impositionServiceProxy") ImpositionServiceInterface impositionService) {
        this.impositionService = impositionService;
    }

    @GetMapping(value = "/{impositionId}")
    @Operation(summary = "Returns the Imposition for the given impositionId.")
    public ResponseEntity<ImpositionEntity> getImpositionById(@PathVariable Long impositionId) {

        log.info(":GET:getImpositionById: impositionId: {}", impositionId);

        ImpositionEntity response = impositionService.getImposition(impositionId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Impositions based upon criteria in request body")
    public ResponseEntity<List<ImpositionEntity>> postImpositionsSearch(@RequestBody ImpositionSearchDto criteria) {
        log.info(":POST:postImpositionsSearch: query: \n{}", criteria);

        List<ImpositionEntity> response = impositionService.searchImpositions(criteria);

        return buildResponse(response);
    }


}
