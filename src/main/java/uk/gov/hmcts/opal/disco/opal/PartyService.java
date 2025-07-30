package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartySummary;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.jpa.PartySpecs;
import uk.gov.hmcts.opal.disco.PartyServiceInterface;
import uk.gov.hmcts.opal.dto.search.DefendantDto;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Qualifier("partyService")
public class PartyService implements PartyServiceInterface {

    private final PartyRepository partyRepository;

    private final PartySpecs specs = new PartySpecs();

    @Override
    public PartyDto getParty(long partyId) {
        return toDto(partyRepository.getReferenceById(partyId));
    }

    @Override
    public PartyDto saveParty(PartyDto partyDto) {
        return toDto(partyRepository.save(toEntity(partyDto)));
    }

    public List<PartySummary> searchForParty(AccountSearchDto accountSearchDto) {
        String surname = Optional.ofNullable(accountSearchDto.getDefendant())
            .map(DefendantDto::getSurname)
            .orElse(null);
        return partyRepository.findBySurnameContaining(surname);
    }

    @Override
    public List<PartyEntity> searchParties(PartySearchDto criteria) {
        Page<PartyEntity> page = partyRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }


}
