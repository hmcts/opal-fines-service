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
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;
import uk.gov.hmcts.opal.disco.ConfigurationItemServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/configuration-items")
@Slf4j(topic = "ConfigurationItemController")
@Tag(name = "ConfigurationItem Controller")
public class ConfigurationItemController {

    private final ConfigurationItemServiceInterface configurationItemService;

    public ConfigurationItemController(
        @Qualifier("configurationItemServiceProxy") ConfigurationItemServiceInterface configurationItemService) {
        this.configurationItemService = configurationItemService;
    }

    @GetMapping(value = "/{configurationItemId}")
    @Operation(summary = "Returns the ConfigurationItem for the given configurationItemId.")
    public ResponseEntity<ConfigurationItemEntity> getConfigurationItemById(@PathVariable Long configurationItemId) {

        log.debug(":GET:getConfigurationItemById: configurationItemId: {}", configurationItemId);

        ConfigurationItemEntity response = configurationItemService.getConfigurationItem(configurationItemId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches ConfigurationItems based upon criteria in request body")
    public ResponseEntity<List<ConfigurationItemEntity>> postConfigurationItemsSearch(
        @RequestBody ConfigurationItemSearchDto criteria) {
        log.debug(":POST:postConfigurationItemsSearch: query: \n{}", criteria);

        List<ConfigurationItemEntity> response = configurationItemService.searchConfigurationItems(criteria);

        return buildResponse(response);
    }


}
