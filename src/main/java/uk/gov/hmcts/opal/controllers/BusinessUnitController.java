package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceData;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;
import uk.gov.hmcts.opal.service.UserStateService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;
import static uk.gov.hmcts.opal.util.PermissionUtil.filterBusinessUnitsByPermission;


@RestController
@RequestMapping("/business-units")
@Slf4j(topic = "opal.BusinessUnitController")
@Tag(name = "BusinessUnit Controller")
public class BusinessUnitController {

    private final BusinessUnitService businessUnitService;

    private final UserStateService userStateService;

    public BusinessUnitController(BusinessUnitService businessUnitService, UserStateService userStateService) {
        this.businessUnitService = businessUnitService;
        this.userStateService = userStateService;
    }

    @GetMapping(value = "/{businessUnitId}")
    @Operation(summary = "Returns the BusinessUnit for the given businessUnitId.")
    public ResponseEntity<BusinessUnitFullEntity> getBusinessUnitById(@PathVariable Short businessUnitId) {

        log.debug(":GET:getBusinessUnitById: businessUnitId: {}", businessUnitId);

        BusinessUnitFullEntity response = businessUnitService.getBusinessUnit(businessUnitId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches BusinessUnits based upon criteria in request body")
    public ResponseEntity<List<BusinessUnitFullEntity>> postBusinessUnitsSearch(
        @RequestBody BusinessUnitSearchDto criteria) {
        log.debug(":POST:postBusinessUnitsSearch: query: \n{}", criteria);

        List<BusinessUnitFullEntity> response = businessUnitService.searchBusinessUnits(criteria);

        return buildResponse(response);
    }

    @GetMapping
    @Operation(summary = "Returns Business Units as reference data with an optional filter applied")
    public ResponseEntity<BusinessUnitReferenceDataResults> getBusinessUnitRefData(
        @RequestParam("q") Optional<String> filter, @RequestParam Optional<FinesPermission> permission,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":GET:getBusinessUnitRefData: permission: {}, query: \n{}", permission, filter);

        List<BusinessUnitReferenceData> refData =  filterBusinessUnitsByPermission(
            userStateService, businessUnitService.getReferenceData(filter), permission, authHeaderValue);

        log.debug(":GET:getBusinessUnitRefData: business unit reference data count: {}", refData.size());
        return ResponseEntity.ok(BusinessUnitReferenceDataResults.builder().refData(refData).build());
    }
}
