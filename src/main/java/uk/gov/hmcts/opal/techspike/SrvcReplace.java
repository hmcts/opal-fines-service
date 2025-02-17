package uk.gov.hmcts.opal.techspike;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.controllers.DraftAccountController;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "TechSpike")
public class SrvcReplace extends TechSpikeAction {

    private static final AtomicInteger count = new AtomicInteger();

    private final int index = count.incrementAndGet();

    private final List<Long> draftIds;

    public SrvcReplace(DraftAccountService draftAccountService, DraftAccountController controller,
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
        long slept = sleep(rndSleep);

        long id = draftIds.get(0);
        long start = System.currentTimeMillis();
        DraftAccountEntity entity = getDraftAccountEntity(id, slept);
        sleep(20);
        entity.setAccountType(getUpdateString(entity));
        DraftAccountEntity replaced = replaceDraftAccount(id, entity, slept);
        long delta = timeMe(start);

        return successCallResponse("Service Replaced '", replaced, delta, slept);
    }
}
