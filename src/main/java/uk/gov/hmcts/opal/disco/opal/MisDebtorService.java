package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.repository.MisDebtorRepository;
import uk.gov.hmcts.opal.repository.jpa.MisDebtorSpecs;
import uk.gov.hmcts.opal.disco.MisDebtorServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("misDebtorService")
public class MisDebtorService implements MisDebtorServiceInterface {

    private final MisDebtorRepository misDebtorRepository;

    private final MisDebtorSpecs specs = new MisDebtorSpecs();

    @Override
    public MisDebtorEntity getMisDebtor(long misDebtorId) {
        return misDebtorRepository.getReferenceById(misDebtorId);
    }

    @Override
    public List<MisDebtorEntity> searchMisDebtors(MisDebtorSearchDto criteria) {
        Page<MisDebtorEntity> page = misDebtorRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
