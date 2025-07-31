package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.jpa.AliasSpecs;
import uk.gov.hmcts.opal.disco.AliasServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("aliasService")
public class AliasService implements AliasServiceInterface {

    private final AliasRepository aliasRepository;

    private final AliasSpecs specs = new AliasSpecs();

    @Override
    public AliasEntity getAlias(long aliasId) {
        return aliasRepository.getReferenceById(aliasId);
    }

    @Override
    public List<AliasEntity> searchAliass(AliasSearchDto criteria) {
        Page<AliasEntity> page = aliasRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
