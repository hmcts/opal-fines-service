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
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.disco.BusinessUnitUserServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/business-unit-users")
@Slf4j(topic = "BusinessUnitUserController")
@Tag(name = "BusinessUnitUser Controller")
public class BusinessUnitUserController {

    private final BusinessUnitUserServiceInterface businessUnitUserService;

    public BusinessUnitUserController(
        @Qualifier("businessUnitUserServiceProxy") BusinessUnitUserServiceInterface businessUnitUserService) {
        this.businessUnitUserService = businessUnitUserService;
    }

    @GetMapping(value = "/{businessUnitUserId}")
    @Operation(summary = "Returns the BusinessUnitUser for the given businessUnitUserId.")
    public ResponseEntity<BusinessUnitUserEntity> getBusinessUnitUserById(@PathVariable String businessUnitUserId) {

        log.debug(":GET:getBusinessUnitUserById: businessUnitUserId: {}", businessUnitUserId);

        BusinessUnitUserEntity response = businessUnitUserService.getBusinessUnitUser(businessUnitUserId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches BusinessUnitUsers based upon criteria in request body")
    public ResponseEntity<List<BusinessUnitUserEntity>> postBusinessUnitUsersSearch(
        @RequestBody BusinessUnitUserSearchDto criteria) {
        log.debug(":POST:postBusinessUnitUsersSearch: query: \n{}", criteria);

        List<BusinessUnitUserEntity> response = businessUnitUserService.searchBusinessUnitUsers(criteria);

        log.debug(":POST:postBusinessUnitUsersSearch: response count: {}", response.size());

        return buildResponse(response);
    }


}
