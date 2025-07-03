package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.reference.ProsecutorReferenceData;
import uk.gov.hmcts.opal.dto.response.RefDataResponse;
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.service.opal.ProsecutorService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/prosecutors")
@Slf4j(topic = "opal.ProsecutorController")
@Tag(name = "Prosecutor Controller")
public class ProscutorController {

    private final ProsecutorService prosecutorservice;

    public ProscutorController(ProsecutorService prosecutorservice) {
        this.prosecutorservice = prosecutorservice;
    }

    @GetMapping(value = "/{prosecutorId}")
    @Operation(summary = "Returns the Prosecutor for the given prosecutorId.")
    public ResponseEntity<ProsecutorEntity> getProsecutorById(@PathVariable Long prosecutorId) {

        log.debug(":GET:getProsecutorById: prosecutorId: {}", prosecutorId);

        ProsecutorEntity response = prosecutorservice.getProsecutorById(prosecutorId);

        return buildResponse(response);
    }

    @GetMapping
    @Operation(summary = "Returns Prosecutors as reference data with an optional filter applied")
    public ResponseEntity<RefDataResponse<ProsecutorReferenceData>> getProsecutorsRefData(
        @RequestParam("q") Optional<String> filter) {
        log.debug(":GET:getProsecutorsRefData: query: \n{}", filter);

        List<ProsecutorReferenceData> refData = prosecutorservice.getReferenceData(filter);

        log.debug(":GET:getProsecutorsRefData: prosecutor reference data count: {}", refData.size());
        return ResponseEntity.ok(RefDataResponse.<ProsecutorReferenceData>builder().refData(refData).build());
    }
}
