package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;

import java.util.List;

public interface TillServiceInterface {

    TillEntity getTill(long tillId);

    List<TillEntity> searchTills(TillSearchDto criteria);
}
