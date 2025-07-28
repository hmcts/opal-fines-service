package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;

import java.util.List;

public interface MiscellaneousAccountServiceInterface {

    MiscellaneousAccountEntity getMiscellaneousAccount(long miscellaneousAccountId);

    List<MiscellaneousAccountEntity> searchMiscellaneousAccounts(MiscellaneousAccountSearchDto criteria);
}
