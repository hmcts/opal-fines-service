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
import uk.gov.hmcts.opal.dto.search.TemplateSearchDto;
import uk.gov.hmcts.opal.entity.TemplateEntity;
import uk.gov.hmcts.opal.service.TemplateServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.ResponseUtil.buildResponse;


@RestController
@RequestMapping("/api/template")
@Slf4j(topic = "TemplateController")
@Tag(name = "Template Controller")
public class TemplateController {

    private final TemplateServiceInterface templateService;

    public TemplateController(@Qualifier("templateServiceProxy") TemplateServiceInterface templateService) {
        this.templateService = templateService;
    }

    @GetMapping(value = "/{templateId}")
    @Operation(summary = "Returns the Template for the given templateId.")
    public ResponseEntity<TemplateEntity> getTemplateById(@PathVariable Long templateId) {

        log.info(":GET:getTemplateById: templateId: {}", templateId);

        TemplateEntity response = templateService.getTemplate(templateId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Templates based upon criteria in request body")
    public ResponseEntity<List<TemplateEntity>> postTemplatesSearch(@RequestBody TemplateSearchDto criteria) {
        log.info(":POST:postTemplatesSearch: query: \n{}", criteria);

        List<TemplateEntity> response = templateService.searchTemplates(criteria);

        log.info(":POST:postTemplatesSearch: response count: {}", response.size());

        return buildResponse(response);
    }


}
