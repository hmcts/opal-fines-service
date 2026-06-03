package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorAccountAtAGlanceEntity;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;
import uk.gov.hmcts.opal.mapper.MajorCreditorAccountHeaderEntityMapper;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorAccountAtAGlanceRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.service.iface.MajorCreditorAccountServiceInterface;

@Service
@Slf4j(topic = "opal.OpalMajorCreditorAccountService")
@RequiredArgsConstructor
public class OpalMajorCreditorAccountService implements MajorCreditorAccountServiceInterface {

    private final MajorCreditorAccountHeaderRepository majorCreditorAccountHeaderRepository;
    private final MajorCreditorAccountHeaderEntityMapper majorCreditorAccountHeaderEntityMapper;
    private final CreditorAccountRepository creditorAccountRepository;
    private final MajorCreditorAccountAtAGlanceRepository majorCreditorAccountAtAGlanceRepository;

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

    @Transactional(readOnly = true)
    public GetMajorCreditorAccountAtAGlanceResponse getAtAGlance(Long creditorAccountId) {
        log.debug(":getAtAGlance (Opal): creditorAccountId={}", creditorAccountId);

        CreditorAccountEntity creditorAccount = creditorAccountRepository.findFullByCreditorAccountId(creditorAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Major creditor account not found: " + creditorAccountId
            ));

        CreditorAccountType accountType = creditorAccount.getCreditorAccountType();
        if (accountType == null || !(accountType.isMajorCreditor() || accountType.isCentralFund())) {
            throw new EntityNotFoundException("Account is not a major creditor account: " + creditorAccountId);
        }

        MajorCreditorAccountAtAGlanceEntity entity = majorCreditorAccountAtAGlanceRepository.findById(creditorAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Major creditor at a glance not found: " + creditorAccountId
            ));

        return GetMajorCreditorAccountAtAGlanceResponse.builder()
            .version(creditorAccount.getVersion())
            .majorCreditor(buildMajorCreditor(entity, creditorAccount, accountType))
            .build();
    }

    private GetMajorCreditorAccountAtAGlanceResponse.MajorCreditor buildMajorCreditor(
        MajorCreditorAccountAtAGlanceEntity entity,
        CreditorAccountEntity creditorAccount,
        CreditorAccountType accountType
    ) {
        return GetMajorCreditorAccountAtAGlanceResponse.MajorCreditor.builder()
            .creditorAccountId(entity.getCreditorAccountId())
            .name(entity.getName())
            .code(getMajorCreditorCode(creditorAccount, accountType))
            .address(buildAddress(entity))
            .payByBacs(accountType.isMajorCreditor() ? creditorAccount.isPayByBacs() : null)
            .build();
    }

    private String getMajorCreditorCode(CreditorAccountEntity creditorAccount, CreditorAccountType accountType) {
        if (!accountType.isMajorCreditor()) {
            return null;
        }

        MajorCreditorEntity majorCreditor = creditorAccount.getMajorCreditor();
        return majorCreditor != null ? majorCreditor.getMajorCreditorCode() : null;
    }

    private AddressDetails buildAddress(MajorCreditorAccountAtAGlanceEntity entity) {
        if (hasNoAddress(entity)) {
            return null;
        }

        return AddressDetails.builder()
            .addressLine1(entity.getAddressLine1())
            .addressLine2(entity.getAddressLine2())
            .addressLine3(entity.getAddressLine3())
            .postcode(entity.getPostcode())
            .build();
    }

    private boolean hasNoAddress(MajorCreditorAccountAtAGlanceEntity entity) {
        return Objects.isNull(entity.getAddressLine1())
            && Objects.isNull(entity.getAddressLine2())
            && Objects.isNull(entity.getAddressLine3())
            && Objects.isNull(entity.getPostcode());
    }
}
