package uk.gov.hmcts.opal.techspike;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.controllers.DraftAccountController;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "TechSpike")
public class HttpUpdate extends TechSpikeAction {

    private static final AtomicInteger count = new AtomicInteger();

    private final int index = count.incrementAndGet();

    private final List<Long> draftIds;

    public HttpUpdate(DraftAccountService draftAccountService, DraftAccountController controller,
                      RestClient restClient, List<Long> draftIds) {
        super(draftAccountService, controller, restClient, 170);
        this.draftIds = draftIds;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public CallResponse doIt() {
        final long slept = sleep(rndSleep);

        final long id = draftIds.get(0);
        final long start = System.currentTimeMillis();
        DraftAccountEntity entity = getDraftAccountEntityHttp(id, slept);
        sleep(20);
        entity.setValidatedByName(getUpdateString(entity));
        entity.setAccountStatus(DraftAccountStatus.PENDING);
        DraftAccountEntity updated = updateDraftAccountHttp(id, entity, slept);
        long delta = timeMe(start);

        return successCallResponse("Service Updated '", updated, delta, slept);
    }
}
