package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;

import java.util.List;

public interface MisDebtorServiceInterface {

    MisDebtorEntity getMisDebtor(long misDebtorId);

    List<MisDebtorEntity> searchMisDebtors(MisDebtorSearchDto criteria);
}
