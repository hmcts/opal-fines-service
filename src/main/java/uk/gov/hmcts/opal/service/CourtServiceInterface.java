package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;

import java.util.List;

public interface CourtServiceInterface {

    CourtEntity getCourt(long courtId);

    List<CourtEntity> searchCourts(CourtSearchDto criteria);
}
