package uk.gov.hmcts.opal.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.AlternatePaymentReferenceDto;
import uk.gov.hmcts.opal.service.AlternatePaymentReferenceService;

@RestController
@RequestMapping("/defendant-accounts")
@Slf4j(topic = "opal.AlternatePaymentReferenceController")
@ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class AlternatePaymentReferenceController {

    private final AlternatePaymentReferenceService alternatePaymentReferenceService;

    @GetMapping("/{defendantAccountId}/alternate-payment-references")
    public ResponseEntity<List<AlternatePaymentReferenceDto>> getAlternatePaymentReferences(
        @PathVariable Long defendantAccountId) {

        return ResponseEntity.ok(alternatePaymentReferenceService.getAlternatePaymentReferences(defendantAccountId));
    }

}
