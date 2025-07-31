package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;

import java.util.List;

public interface CommittalWarrantProgressServiceInterface {

    CommittalWarrantProgressEntity getCommittalWarrantProgress(long committalWarrantProgressId);

    List<CommittalWarrantProgressEntity> searchCommittalWarrantProgresss(CommittalWarrantProgressSearchDto criteria);
}
