package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.TillServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TillService implements TillServiceInterface {

    private final TillRepository tillRepository;

    @Override
    public TillEntity getTill(long tillId) {
        return tillRepository.getReferenceById(tillId);
    }

    @Override
    public List<TillEntity> searchTills(TillSearchDto criteria) {
        return null;
    }

}
