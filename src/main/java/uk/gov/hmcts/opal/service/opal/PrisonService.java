package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.repository.PrisonRepository;
import uk.gov.hmcts.opal.service.PrisonServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrisonService implements PrisonServiceInterface {

    private final PrisonRepository prisonRepository;

    @Override
    public PrisonEntity getPrison(long prisonId) {
        return prisonRepository.getReferenceById(prisonId);
    }

    @Override
    public List<PrisonEntity> searchPrisons(PrisonSearchDto criteria) {
        return null;
    }

}
