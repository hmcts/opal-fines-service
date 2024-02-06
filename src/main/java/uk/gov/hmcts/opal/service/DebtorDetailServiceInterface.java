package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;

import java.util.List;

public interface DebtorDetailServiceInterface {

    DebtorDetailEntity getDebtorDetail(long debtorDetailId);

    List<DebtorDetailEntity> searchDebtorDetails(DebtorDetailSearchDto criteria);
}
