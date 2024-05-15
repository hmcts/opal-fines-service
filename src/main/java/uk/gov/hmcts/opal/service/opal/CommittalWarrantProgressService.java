package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.repository.CommittalWarrantProgressRepository;
import uk.gov.hmcts.opal.repository.jpa.CommittalWarrantProgressSpecs;
import uk.gov.hmcts.opal.service.CommittalWarrantProgressServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("committalWarrantProgressService")
public class CommittalWarrantProgressService implements CommittalWarrantProgressServiceInterface {

    private final CommittalWarrantProgressRepository committalWarrantProgressRepository;

    private final CommittalWarrantProgressSpecs specs = new CommittalWarrantProgressSpecs();

    @Override
    public CommittalWarrantProgressEntity getCommittalWarrantProgress(long committalWarrantProgressId) {
        return committalWarrantProgressRepository.getReferenceById(committalWarrantProgressId);
    }

    @Override
    public List<CommittalWarrantProgressEntity> searchCommittalWarrantProgresss(
        CommittalWarrantProgressSearchDto criteria) {
        Page<CommittalWarrantProgressEntity> page = committalWarrantProgressRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
