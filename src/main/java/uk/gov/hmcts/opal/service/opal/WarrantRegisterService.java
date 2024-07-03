package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.WarrantRegisterSearchDto;
import uk.gov.hmcts.opal.entity.WarrantRegisterEntity;
import uk.gov.hmcts.opal.repository.WarrantRegisterRepository;
import uk.gov.hmcts.opal.repository.jpa.WarrantRegisterSpecs;
import uk.gov.hmcts.opal.service.WarrantRegisterServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("warrantRegisterService")
public class WarrantRegisterService implements WarrantRegisterServiceInterface {

    private final WarrantRegisterRepository warrantRegisterRepository;

    private final WarrantRegisterSpecs specs = new WarrantRegisterSpecs();

    @Override
    public WarrantRegisterEntity getWarrantRegister(long warrantRegisterId) {
        return warrantRegisterRepository.getReferenceById(warrantRegisterId);
    }

    @Override
    public List<WarrantRegisterEntity> searchWarrantRegisters(WarrantRegisterSearchDto criteria) {
        Page<WarrantRegisterEntity> page = warrantRegisterRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
