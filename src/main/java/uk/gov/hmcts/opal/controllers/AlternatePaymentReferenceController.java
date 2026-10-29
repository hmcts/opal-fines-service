package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.service.AlternatePaymentReferenceService;

@RestController

@RequiredArgsConstructor
@Slf4j(topic = "opal.AlternatePaymentReferenceController")
@Tag(name = "Alternate Payment Reference Controller")
public class AlternatePaymentReferenceController {

    private final AlternatePaymentReferenceService alternatePaymentReferenceService;

}