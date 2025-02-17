package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.ConfigurationItemLite;

import java.util.List;

public interface ConfigurationItemServiceInterface {

    ConfigurationItemLite getConfigurationItem(long configurationItemId);

    List<ConfigurationItemLite> searchConfigurationItems(ConfigurationItemSearchDto criteria);
}
