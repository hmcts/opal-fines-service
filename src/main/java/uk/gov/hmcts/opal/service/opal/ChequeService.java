package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ChequeSearchDto;
import uk.gov.hmcts.opal.entity.ChequeEntity;
import uk.gov.hmcts.opal.repository.ChequeRepository;
import uk.gov.hmcts.opal.repository.jpa.ChequeSpecs;
import uk.gov.hmcts.opal.service.ChequeServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("chequeService")
public class ChequeService implements ChequeServiceInterface {

    private final ChequeRepository chequeRepository;

    private final ChequeSpecs specs = new ChequeSpecs();

    @Override
    public ChequeEntity getCheque(long chequeId) {
        return chequeRepository.getReferenceById(chequeId);
    }

    @Override
    public List<ChequeEntity> searchCheques(ChequeSearchDto criteria) {
        Page<ChequeEntity> page = chequeRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
