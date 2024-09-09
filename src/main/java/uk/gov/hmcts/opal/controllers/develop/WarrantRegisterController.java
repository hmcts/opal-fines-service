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
import uk.gov.hmcts.opal.dto.search.WarrantRegisterSearchDto;
import uk.gov.hmcts.opal.entity.WarrantRegisterEntity;
import uk.gov.hmcts.opal.service.WarrantRegisterServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/warrant-registers")
@Slf4j(topic = "WarrantRegisterController")
@Tag(name = "WarrantRegister Controller")
public class WarrantRegisterController {

    private final WarrantRegisterServiceInterface warrantRegisterService;

    public WarrantRegisterController(@Qualifier("warrantRegisterService")
                                     WarrantRegisterServiceInterface warrantRegisterService) {
        this.warrantRegisterService = warrantRegisterService;
    }

    @GetMapping(value = "/{warrantRegisterId}")
    @Operation(summary = "Returns the WarrantRegister for the given warrantRegisterId.")
    public ResponseEntity<WarrantRegisterEntity> getWarrantRegisterById(@PathVariable Long warrantRegisterId) {

        log.info(":GET:getWarrantRegisterById: warrantRegisterId: {}", warrantRegisterId);

        WarrantRegisterEntity response = warrantRegisterService.getWarrantRegister(warrantRegisterId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Warrant Registers based upon criteria in request body")
    public ResponseEntity<List<WarrantRegisterEntity>> postWarrantRegistersSearch(
        @RequestBody WarrantRegisterSearchDto criteria) {
        log.info(":POST:postWarrantRegistersSearch: query: \n{}", criteria);

        List<WarrantRegisterEntity> response = warrantRegisterService.searchWarrantRegisters(criteria);

        return buildResponse(response);
    }


}
