package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.ConfigurationItemLite;
import uk.gov.hmcts.opal.repository.ConfigurationItemRepository;
import uk.gov.hmcts.opal.repository.jpa.ConfigurationItemSpecs;
import uk.gov.hmcts.opal.service.ConfigurationItemServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("logAuditDetailService")
public class ConfigurationItemService implements ConfigurationItemServiceInterface {

    private final ConfigurationItemRepository configurationItemRepository;

    private final ConfigurationItemSpecs specs = new ConfigurationItemSpecs();

    @Override
    public ConfigurationItemLite getConfigurationItem(long configurationItemId) {
        return configurationItemRepository.getReferenceById(configurationItemId);
    }

    @Override
    public List<ConfigurationItemLite> searchConfigurationItems(ConfigurationItemSearchDto criteria) {
        Page<ConfigurationItemLite> page = configurationItemRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
