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
import uk.gov.hmcts.opal.dto.search.SuspenseItemSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.service.SuspenseItemServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/suspense-items")
@Slf4j(topic = "SuspenseItemController")
@Tag(name = "Suspense Item Controller")
public class SuspenseItemController {

    private final SuspenseItemServiceInterface suspenseItemService;

    public SuspenseItemController(@Qualifier("suspenseItemServiceProxy")
                                  SuspenseItemServiceInterface suspenseItemService) {
        this.suspenseItemService = suspenseItemService;
    }

    @GetMapping(value = "/{suspenseItemId}")
    @Operation(summary = "Returns the SuspenseItem for the given suspenseItemId.")
    public ResponseEntity<SuspenseItemEntity> getSuspenseItemById(@PathVariable Long suspenseItemId) {

        log.info(":GET:getSuspenseItemById: suspenseItemId: {}", suspenseItemId);

        SuspenseItemEntity response = suspenseItemService.getSuspenseItem(suspenseItemId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches SuspenseItems based upon criteria in request body")
    public ResponseEntity<List<SuspenseItemEntity>> postSuspenseItemsSearch(
        @RequestBody SuspenseItemSearchDto criteria) {
        log.info(":POST:postSuspenseItemsSearch: query: \n{}", criteria);

        List<SuspenseItemEntity> response = suspenseItemService.searchSuspenseItems(criteria);

        return buildResponse(response);
    }


}
