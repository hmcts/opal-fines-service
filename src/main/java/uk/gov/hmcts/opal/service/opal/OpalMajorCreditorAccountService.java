package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.mapper.MajorCreditorAccountHeaderEntityMapper;
import uk.gov.hmcts.opal.repository.MajorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.service.iface.MajorCreditorAccountServiceInterface;

@Service
@Slf4j(topic = "opal.OpalMajorCreditorAccountService")
@RequiredArgsConstructor
public class OpalMajorCreditorAccountService implements MajorCreditorAccountServiceInterface {

    private final MajorCreditorAccountHeaderRepository majorCreditorAccountHeaderRepository;
    private final MajorCreditorAccountHeaderEntityMapper majorCreditorAccountHeaderEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public GetMajorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long majorCreditorAccountId) {
        log.debug(":getHeaderSummary (Opal): majorCreditorAccountId={}", majorCreditorAccountId);

        return majorCreditorAccountHeaderRepository.findById(majorCreditorAccountId)
            .map(majorCreditorAccountHeaderEntityMapper::toResponse)
            .orElseThrow(() -> new EntityNotFoundException(
                "Major creditor account not found: " + majorCreditorAccountId
            ));
    }
}
