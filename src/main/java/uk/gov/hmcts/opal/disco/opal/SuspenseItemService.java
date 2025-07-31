package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.SuspenseItemSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.repository.SuspenseItemRepository;
import uk.gov.hmcts.opal.repository.jpa.SuspenseItemSpecs;
import uk.gov.hmcts.opal.disco.SuspenseItemServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("suspenseItemService")
public class SuspenseItemService implements SuspenseItemServiceInterface {

    private final SuspenseItemRepository suspenseItemRepository;

    private final SuspenseItemSpecs specs = new SuspenseItemSpecs();

    @Override
    public SuspenseItemEntity getSuspenseItem(long suspenseItemId) {
        return suspenseItemRepository.getReferenceById(suspenseItemId);
    }

    @Override
    public List<SuspenseItemEntity> searchSuspenseItems(SuspenseItemSearchDto criteria) {
        Page<SuspenseItemEntity> page = suspenseItemRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
