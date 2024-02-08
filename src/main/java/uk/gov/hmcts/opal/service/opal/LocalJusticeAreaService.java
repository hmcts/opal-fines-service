package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.service.LocalJusticeAreaServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalJusticeAreaService implements LocalJusticeAreaServiceInterface {

    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    @Override
    public LocalJusticeAreaEntity getLocalJusticeArea(long localJusticeAreaId) {
        return localJusticeAreaRepository.getReferenceById(localJusticeAreaId);
    }

    @Override
    public List<LocalJusticeAreaEntity> searchLocalJusticeAreas(LocalJusticeAreaSearchDto criteria) {
        return null;
    }

}
