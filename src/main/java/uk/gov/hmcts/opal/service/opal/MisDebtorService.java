package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.repository.MisDebtorRepository;
import uk.gov.hmcts.opal.service.MisDebtorServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MisDebtorService implements MisDebtorServiceInterface {

    private final MisDebtorRepository misDebtorRepository;

    @Override
    public MisDebtorEntity getMisDebtor(long misDebtorId) {
        return misDebtorRepository.getReferenceById(misDebtorId);
    }

    @Override
    public List<MisDebtorEntity> searchMisDebtors(MisDebtorSearchDto criteria) {
        return null;
    }

}
