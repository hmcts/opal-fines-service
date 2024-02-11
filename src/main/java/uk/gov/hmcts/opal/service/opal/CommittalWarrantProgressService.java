package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.repository.CommittalWarrantProgressRepository;
import uk.gov.hmcts.opal.service.CommittalWarrantProgressServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommittalWarrantProgressService implements CommittalWarrantProgressServiceInterface {

    private final CommittalWarrantProgressRepository committalWarrantProgressRepository;

    @Override
    public CommittalWarrantProgressEntity getCommittalWarrantProgress(long committalWarrantProgressId) {
        return committalWarrantProgressRepository.getReferenceById(committalWarrantProgressId);
    }

    @Override
    public List<CommittalWarrantProgressEntity> searchCommittalWarrantProgresss(
        CommittalWarrantProgressSearchDto criteria) {
        return null;
    }

}
