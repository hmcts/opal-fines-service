package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;

import java.util.List;

public interface ConfigurationItemServiceInterface {

    ConfigurationItemEntity getConfigurationItem(long configurationItemId);

    List<ConfigurationItemEntity> searchConfigurationItems(ConfigurationItemSearchDto criteria);
}
