package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;

import java.util.List;

public interface ResultServiceInterface {

    ResultEntity getResult(long resultId);

    List<ResultEntity> searchResults(ResultSearchDto criteria);
}
