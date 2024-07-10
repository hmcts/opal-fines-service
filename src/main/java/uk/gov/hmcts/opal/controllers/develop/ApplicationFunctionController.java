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
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.service.ApplicationFunctionServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/application-function")
@Slf4j(topic = "ApplicationFunctionController")
@Tag(name = "ApplicationFunction Controller")
public class ApplicationFunctionController {

    private final ApplicationFunctionServiceInterface applicationFunctionService;

    public ApplicationFunctionController(
        @Qualifier("applicationFunctionServiceProxy") ApplicationFunctionServiceInterface applicationFunctionService) {
        this.applicationFunctionService = applicationFunctionService;
    }

    @GetMapping(value = "/{applicationFunctionId}")
    @Operation(summary = "Returns the ApplicationFunction for the given applicationFunctionId.")
    public ResponseEntity<ApplicationFunctionEntity> getApplicationFunctionById(
        @PathVariable Long applicationFunctionId) {

        log.info(":GET:getApplicationFunctionById: applicationFunctionId: {}", applicationFunctionId);

        ApplicationFunctionEntity response = applicationFunctionService.getApplicationFunction(applicationFunctionId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches ApplicationFunctions based upon criteria in request body")
    public ResponseEntity<List<ApplicationFunctionEntity>> postApplicationFunctionsSearch(
        @RequestBody ApplicationFunctionSearchDto criteria) {
        log.info(":POST:postApplicationFunctionsSearch: query: \n{}", criteria);

        List<ApplicationFunctionEntity> response = applicationFunctionService.searchApplicationFunctions(criteria);

        log.info(":POST:postApplicationFunctionsSearch: response count: {}", response.size());

        return buildResponse(response);
    }


}
