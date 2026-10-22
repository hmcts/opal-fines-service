package uk.gov.hmcts.opal.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.repository.AlternatePaymentReferenceRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.AlternatePaymentReferenceController")
public class AlternatePaymentReferenceService {

    private final AlternatePaymentReferenceRepository repository;

}