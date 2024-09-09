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
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.service.TemplateMappingServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/template-mappings")
@Slf4j(topic = "TemplateMappingController")
@Tag(name = "TemplateMapping Controller")
public class TemplateMappingController {

    private final TemplateMappingServiceInterface templateMappingService;

    public TemplateMappingController(
        @Qualifier("templateMappingServiceProxy") TemplateMappingServiceInterface templateMappingService) {
        this.templateMappingService = templateMappingService;
    }

    @GetMapping(value = "/{templateId}/{applicationFunctionId}")
    @Operation(summary = "Returns the TemplateMapping for the given templateMappingId.")
    public ResponseEntity<TemplateMappingEntity> getTemplateMappingById(@PathVariable Long templateId,
                                                                        @PathVariable Long applicationFunctionId) {

        log.info(":GET:getTemplateMappingById: templateId: {}, applicationFunctionId: {}",
                 templateId, applicationFunctionId);

        TemplateMappingEntity response = templateMappingService.getTemplateMapping(templateId, applicationFunctionId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches TemplateMappings based upon criteria in request body")
    public ResponseEntity<List<TemplateMappingEntity>> postTemplateMappingsSearch(
        @RequestBody TemplateMappingSearchDto criteria) {
        log.info(":POST:postTemplateMappingsSearch: query: \n{}", criteria);

        List<TemplateMappingEntity> response = templateMappingService.searchTemplateMappings(criteria);

        log.info(":POST:postTemplateMappingsSearch: response count: {}", response.size());

        return buildResponse(response);
    }


}
