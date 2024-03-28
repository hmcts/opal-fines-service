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
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.service.DebtorDetailServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/debtor-detail")
@Slf4j(topic = "DebtorDetailController")
@Tag(name = "DebtorDetail Controller")
public class DebtorDetailController {

    private final DebtorDetailServiceInterface debtorDetailService;

    public DebtorDetailController(
        @Qualifier("debtorDetailService") DebtorDetailServiceInterface debtorDetailService) {
        this.debtorDetailService = debtorDetailService;
    }

    @GetMapping(value = "/{debtorDetailId}")
    @Operation(summary = "Returns the DebtorDetail for the given debtorDetailId.")
    public ResponseEntity<DebtorDetailEntity> getDebtorDetailById(@PathVariable Long debtorDetailId) {

        log.info(":GET:getDebtorDetailById: debtorDetailId: {}", debtorDetailId);

        DebtorDetailEntity response = debtorDetailService.getDebtorDetail(debtorDetailId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches DebtorDetails based upon criteria in request body")
    public ResponseEntity<List<DebtorDetailEntity>> postDebtorDetailsSearch(
        @RequestBody DebtorDetailSearchDto criteria) {
        log.info(":POST:postDebtorDetailsSearch: query: \n{}", criteria);

        List<DebtorDetailEntity> response = debtorDetailService.searchDebtorDetails(criteria);

        return buildResponse(response);
    }


}
