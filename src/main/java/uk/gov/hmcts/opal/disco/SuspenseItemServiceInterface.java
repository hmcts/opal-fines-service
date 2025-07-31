package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.SuspenseItemSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;

import java.util.List;

public interface SuspenseItemServiceInterface {

    SuspenseItemEntity getSuspenseItem(long suspenseItemId);

    List<SuspenseItemEntity> searchSuspenseItems(SuspenseItemSearchDto criteria);
}
