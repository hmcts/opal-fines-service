package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.HmrcRequestSearchDto;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity;

import java.util.List;

public interface HmrcRequestServiceInterface {

    HmrcRequestEntity getHmrcRequest(long hmrcRequestId);

    List<HmrcRequestEntity> searchHmrcRequests(HmrcRequestSearchDto criteria);
}
