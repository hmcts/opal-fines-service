package uk.gov.hmcts.opal.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.repository.PartyRepository;

@Service
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "app-mode", havingValue = "opal", matchIfMissing = true)
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


}
