package uk.gov.hmcts.opal.dto;

import uk.gov.hmcts.opal.entity.draft.DraftAccountType;

public interface DraftAccountRequestDto {

    String getAccount();

    DraftAccountType getAccountType();
}
