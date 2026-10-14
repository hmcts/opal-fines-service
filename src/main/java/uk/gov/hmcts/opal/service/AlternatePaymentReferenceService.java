package uk.gov.hmcts.opal.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AlternatePaymentReferenceDto;
import uk.gov.hmcts.opal.mapper.AlternatePaymentReferenceMapper;
import uk.gov.hmcts.opal.repository.AlternatePaymentReferenceRepository;

@Service
@Slf4j(topic = "opal.AlternatePaymentReferenceService")
@RequiredArgsConstructor
public class AlternatePaymentReferenceService {

    private final AlternatePaymentReferenceRepository alternatePaymentReferenceRepository;
    private final AlternatePaymentReferenceMapper alternatePaymentReferenceMapper;

    public List<AlternatePaymentReferenceDto> getAlternatePaymentReferences(Long defendantAccountId) {

        return alternatePaymentReferenceRepository .findByDefendantAccountId(defendantAccountId)
            .stream()
            .map(alternatePaymentReferenceMapper::toDto)
            .toList();
    }
}