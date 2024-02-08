package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;

import java.util.List;

public interface LocalJusticeAreaServiceInterface {

    LocalJusticeAreaEntity getLocalJusticeArea(long localJusticeAreaId);

    List<LocalJusticeAreaEntity> searchLocalJusticeAreas(LocalJusticeAreaSearchDto criteria);
}
