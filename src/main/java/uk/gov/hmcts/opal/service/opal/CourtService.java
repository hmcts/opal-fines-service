package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.service.CourtServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService implements CourtServiceInterface {

    private final CourtRepository courtRepository;

    @Override
    public CourtEntity getCourt(long courtId) {
        return courtRepository.getReferenceById(courtId);
    }

    @Override
    public List<CourtEntity> searchCourts(CourtSearchDto criteria) {
        return null;
    }

}
