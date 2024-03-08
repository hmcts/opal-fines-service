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
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.service.BusinessUnitServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/business-unit")
@Slf4j(topic = "BusinessUnitController")
@Tag(name = "BusinessUnit Controller")
public class BusinessUnitController {

    private final BusinessUnitServiceInterface businessUnitService;

    public BusinessUnitController(
        @Qualifier("businessUnitServiceProxy") BusinessUnitServiceInterface businessUnitService) {
        this.businessUnitService = businessUnitService;
    }

    @GetMapping(value = "/{businessUnitId}")
    @Operation(summary = "Returns the BusinessUnit for the given businessUnitId.")
    public ResponseEntity<BusinessUnitEntity> getBusinessUnitById(@PathVariable Long businessUnitId) {

        log.info(":GET:getBusinessUnitById: businessUnitId: {}", businessUnitId);

        BusinessUnitEntity response = businessUnitService.getBusinessUnit(businessUnitId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches BusinessUnits based upon criteria in request body")
    public ResponseEntity<List<BusinessUnitEntity>> postBusinessUnitsSearch(
        @RequestBody BusinessUnitSearchDto criteria) {
        log.info(":POST:postBusinessUnitsSearch: query: \n{}", criteria);

        List<BusinessUnitEntity> response = businessUnitService.searchBusinessUnits(criteria);

        return buildResponse(response);
    }


}
