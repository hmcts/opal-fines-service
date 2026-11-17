package uk.gov.hmcts.opal.service.opal.till;

import static uk.gov.hmcts.opal.mapper.helper.JsonNullableMapperHelper.fromJsonNullable;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.mapper.till.CreateTillMapper;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;

@Service
@RequiredArgsConstructor
public class CreateTillPayerService {

    private final MiscellaneousAccountRepository miscellaneousAccountRepository;
    private final CreateTillMapper mapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public MiscellaneousAccountEntity createPayerAccount(TillsCreateAdditionalInformation information,
                                                        BusinessUnitEntity businessUnit) {
        if (information.getPayerType() == null) {
            return null;
        }
        return miscellaneousAccountRepository.save(MiscellaneousAccountEntity.builder()
            .businessUnit(businessUnit)
            .accountNumber(miscellaneousAccountRepository.nextAccountNumber(businessUnit.getBusinessUnitId()))
            .party(mapper.toParty(fromJsonNullable(information.getPayerDetails()), information.getPayerType()))
            .build());
    }
}
