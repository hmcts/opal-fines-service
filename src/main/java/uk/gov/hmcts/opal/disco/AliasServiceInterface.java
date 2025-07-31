package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;

import java.util.List;

public interface AliasServiceInterface {

    AliasEntity getAlias(long aliasId);

    List<AliasEntity> searchAliass(AliasSearchDto criteria);
}
