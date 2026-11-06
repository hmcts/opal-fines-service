package uk.gov.hmcts.opal.service.messaging;

import java.util.List;

public interface PublishDraftAccountPublisherInterface {

    void publish(List<Long> draftAccountIds);
}
