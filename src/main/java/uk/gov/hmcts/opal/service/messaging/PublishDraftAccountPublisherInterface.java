package uk.gov.hmcts.opal.service.messaging;

import uk.gov.hmcts.opal.dto.DraftAccountsResponseDto;

public interface PublishDraftAccountPublisherInterface {

    void publish(DraftAccountsResponseDto draftAccounts);
}
