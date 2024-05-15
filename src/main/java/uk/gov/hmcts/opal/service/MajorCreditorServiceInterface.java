package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;

import java.util.List;

public interface MajorCreditorServiceInterface {

    MajorCreditorEntity getMajorCreditor(long majorCreditorId);

    List<MajorCreditorEntity> searchMajorCreditors(MajorCreditorSearchDto criteria);
}
