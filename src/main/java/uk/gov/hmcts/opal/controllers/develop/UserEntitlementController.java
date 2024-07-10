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
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.service.UserEntitlementServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/user-entitlement")
@Slf4j(topic = "UserEntitlementController")
@Tag(name = "UserEntitlement Controller")
public class UserEntitlementController {

    private final UserEntitlementServiceInterface userEntitlementService;

    public UserEntitlementController(
        @Qualifier("userEntitlementServiceProxy") UserEntitlementServiceInterface userEntitlementService) {
        this.userEntitlementService = userEntitlementService;
    }

    @GetMapping(value = "/{userEntitlementId}")
    @Operation(summary = "Returns the UserEntitlement for the given userEntitlementId.")
    public ResponseEntity<UserEntitlementEntity> getUserEntitlementById(@PathVariable Long userEntitlementId) {

        log.info(":GET:getUserEntitlementById: userEntitlementId: {}", userEntitlementId);

        UserEntitlementEntity response = userEntitlementService.getUserEntitlement(userEntitlementId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches UserEntitlements based upon criteria in request body")
    public ResponseEntity<List<UserEntitlementEntity>> postUserEntitlementsSearch(
        @RequestBody UserEntitlementSearchDto criteria) {
        log.info(":POST:postUserEntitlementsSearch: query: \n{}", criteria);

        List<UserEntitlementEntity> response = userEntitlementService.searchUserEntitlements(criteria);

        log.info(":POST:postUserEntitlementsSearch: response count: {}", response.size());

        return buildResponse(response);
    }


}
