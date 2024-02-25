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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.service.CommittalWarrantProgressServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.ResponseUtil.buildResponse;


@RestController
@RequestMapping("/api/committal-warrant-progress")
@Slf4j(topic = "CommittalWarrantProgressController")
@Tag(name = "CommittalWarrantProgress Controller")
public class CommittalWarrantProgressController {

    private final CommittalWarrantProgressServiceInterface committalWarrantProgressService;

    public CommittalWarrantProgressController(@Qualifier("committalWarrantProgressServiceProxy")
                                            CommittalWarrantProgressServiceInterface committalWarrantProgressService) {
        this.committalWarrantProgressService = committalWarrantProgressService;
    }

    @GetMapping(value = "/{committalWarrantProgressId}")
    @Operation(summary = "Returns the CommittalWarrantProgress for the given committalWarrantProgressId.")
    public ResponseEntity<CommittalWarrantProgressEntity> getCommittalWarrantProgressById(
        @PathVariable Long committalWarrantProgressId) {

        log.info(":GET:getCommittalWarrantProgressById: committalWarrantProgressId: {}", committalWarrantProgressId);

        CommittalWarrantProgressEntity response = committalWarrantProgressService.getCommittalWarrantProgress(
            committalWarrantProgressId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches CommittalWarrantProgresss based upon criteria in request body")
    public ResponseEntity<List<CommittalWarrantProgressEntity>> postCommittalWarrantProgresssSearch(
        @RequestBody CommittalWarrantProgressSearchDto criteria) {
        log.info(":POST:postCommittalWarrantProgresssSearch: query: \n{}", criteria);

        List<CommittalWarrantProgressEntity> response = committalWarrantProgressService
            .searchCommittalWarrantProgresss(criteria);

        return buildResponse(response);
    }


}
