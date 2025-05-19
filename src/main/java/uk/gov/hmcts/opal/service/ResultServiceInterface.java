package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntityFull;
import uk.gov.hmcts.opal.entity.result.ResultEntityLite;

import java.util.List;

public interface ResultServiceInterface {

    ResultEntityLite getResult(String resultId);

    List<ResultEntityFull> searchResults(ResultSearchDto criteria);
}
