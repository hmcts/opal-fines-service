package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.entity.PartySummary;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.service.PartyServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartyService implements PartyServiceInterface {

    private final PartyRepository partyRepository;

    @Override
    public PartyDto getParty(long partyId) {
        return toDto(partyRepository.getReferenceById(partyId));
    }

    @Override
    public PartyDto saveParty(PartyDto partyDto) {
        return toDto(partyRepository.save(toEntity(partyDto)));
    }

    public List<PartySummary> searchForParty(AccountSearchDto accountSearchDto) {
        return partyRepository.findBySurnameContaining(accountSearchDto.getSurname());
    }


}
