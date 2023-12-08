package uk.gov.hmcts.opal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

import java.io.InputStream;
import java.util.List;

import static uk.gov.hmcts.opal.dto.ToJsonString.newObjectMapper;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class DefendantAccountService {

    private final DefendantAccountRepository defendantAccountRepository;

    public DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request) {

        return defendantAccountRepository.findByBusinessUnitIdAndAccountNumber(
            request.getBusinessUnitId(), request.getAccountNumber());
    }

    public DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity) {

        return defendantAccountRepository.save(defendantAccountEntity);
    }

    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {

        log.info(":getDefendantAccountsByBusinessUnit: busUnit: {}", businessUnitId);
        return defendantAccountRepository.findAllByBusinessUnitId(businessUnitId);
    }

    public AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {

        if ("test".equalsIgnoreCase(accountSearchDto.getCourt())) {

            try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("tempData.json")) {
                ObjectMapper mapper = newObjectMapper();
                AccountSearchResultsDto dto = mapper.readValue(in, AccountSearchResultsDto.class);
                log.info(":searchDefendantAccounts: temporary Hack for Front End testing. Read JSON file: \n{}",
                         dto.toPrettyJsonString());
                return dto;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return AccountSearchResultsDto.builder()
            .searchResults(List.of(AccountSummaryDto.builder().build()))
            .totalCount(999)
            .cursor(0)
            .build();
    }
}
