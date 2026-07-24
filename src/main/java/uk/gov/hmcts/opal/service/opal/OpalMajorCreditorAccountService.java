package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorAccountAtAGlanceEntity;
import uk.gov.hmcts.opal.mapper.MajorCreditorAccountHeaderEntityMapper;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorAccountAtAGlanceRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.service.iface.MajorCreditorAccountServiceInterface;
import uk.gov.hmcts.opal.service.opal.history.majorcreditor.MajorCreditorHistoryService;

@Service
@Slf4j(topic = "opal.OpalMajorCreditorAccountService")
@RequiredArgsConstructor
public class OpalMajorCreditorAccountService implements MajorCreditorAccountServiceInterface {

    private static final String ACCOUNT_NOT_FOUND = "Major creditor account not found: ";
    private static final String BACS_DETAILS_PROVIDED = "PROVIDED";
    private static final String BACS_DETAILS_NOT_PROVIDED = "NOT PROVIDED";

    private final CreditorAccountRepository creditorAccountRepository;
    private final MajorCreditorAccountAtAGlanceRepository majorCreditorAccountAtAGlanceRepository;
    private final MajorCreditorAccountHeaderRepository majorCreditorAccountHeaderRepository;
    private final MajorCreditorAccountHeaderEntityMapper majorCreditorAccountHeaderEntityMapper;
    private final MajorCreditorHistoryService majorCreditorHistoryService;

    @Override
    @Transactional(readOnly = true)
    public GetMajorCreditorAccountAtAGlanceResponse getAtAGlance(Long majorCreditorAccountId) {
        log.debug(":getAtAGlance (Opal): majorCreditorAccountId={}", majorCreditorAccountId);

        CreditorAccountEntity creditorAccount = creditorAccountRepository
            .findFullByCreditorAccountId(majorCreditorAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                ACCOUNT_NOT_FOUND + majorCreditorAccountId
            ));

        if (creditorAccount.getCreditorAccountType() != CreditorAccountType.MJ
            && creditorAccount.getCreditorAccountType() != CreditorAccountType.CF) {
            throw new EntityNotFoundException(ACCOUNT_NOT_FOUND + majorCreditorAccountId);
        }

        MajorCreditorAccountAtAGlanceEntity atAGlance = majorCreditorAccountAtAGlanceRepository.findById(
            majorCreditorAccountId
        ).orElseThrow(() -> new EntityNotFoundException(
            ACCOUNT_NOT_FOUND + majorCreditorAccountId
        ));

        GetMajorCreditorAccountAtAGlanceResponse.MajorCreditor majorCreditor =
            new GetMajorCreditorAccountAtAGlanceResponse.MajorCreditor();
        majorCreditor.setCreditorAccountId(atAGlance.getCreditorAccountId());
        majorCreditor.setName(atAGlance.getName());
        majorCreditor.setAddress(mapAddress(atAGlance));

        if (creditorAccount.getCreditorAccountType() == CreditorAccountType.MJ) {
            majorCreditor.setCode(creditorAccount.getMajorCreditor().getMajorCreditorCode());
            majorCreditor.setPayByBacs(mapPayByBacs(atAGlance.getBacsDetails()));
        }

        GetMajorCreditorAccountAtAGlanceResponse response = new GetMajorCreditorAccountAtAGlanceResponse();
        response.setMajorCreditor(majorCreditor);
        response.setVersion(creditorAccount.getVersion());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public GetMajorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long majorCreditorAccountId) {
        log.debug(":getHeaderSummary (Opal): majorCreditorAccountId={}", majorCreditorAccountId);

        return majorCreditorAccountHeaderRepository.findById(majorCreditorAccountId)
            .map(majorCreditorAccountHeaderEntityMapper::toResponse)
            .orElseThrow(() -> new EntityNotFoundException(
                ACCOUNT_NOT_FOUND + majorCreditorAccountId
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public GetMajorCreditorHistoryResponse getHistory(
        Long majorCreditorAccountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes
    ) {
        log.debug(":getHistory (Opal): majorCreditorAccountId={}", majorCreditorAccountId);
        return majorCreditorHistoryService.getHistory(majorCreditorAccountId, dateFrom, dateTo, itemTypes);
    }

    private GetMajorCreditorAccountAtAGlanceResponse.Address mapAddress(
        MajorCreditorAccountAtAGlanceEntity atAGlance
    ) {
        if (atAGlance.getAddressLine1() == null
            && atAGlance.getAddressLine2() == null
            && atAGlance.getAddressLine3() == null
            && atAGlance.getPostcode() == null) {
            return null;
        }

        GetMajorCreditorAccountAtAGlanceResponse.Address address =
            new GetMajorCreditorAccountAtAGlanceResponse.Address();
        address.setLine1(atAGlance.getAddressLine1());
        address.setLine2(atAGlance.getAddressLine2());
        address.setLine3(atAGlance.getAddressLine3());
        address.setPostcode(atAGlance.getPostcode());
        return address;
    }

    private Boolean mapPayByBacs(String bacsDetails) {
        if (BACS_DETAILS_PROVIDED.equals(bacsDetails)) {
            return true;
        }
        if (BACS_DETAILS_NOT_PROVIDED.equals(bacsDetails)) {
            return false;
        }
        return null;
    }
}
