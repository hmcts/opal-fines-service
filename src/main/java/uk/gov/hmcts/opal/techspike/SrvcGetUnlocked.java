package uk.gov.hmcts.opal.techspike;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.controllers.DraftAccountController;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "TechSpike")
public class SrvcGetUnlocked extends TechSpikeAction {

    private static final AtomicInteger count = new AtomicInteger();

    private final int index = count.incrementAndGet();

    private final List<Long> draftIds;

    public SrvcGetUnlocked(DraftAccountService draftAccountService, DraftAccountController controller,
                            RestClient restClient, List<Long> draftIds) {
        super(draftAccountService, controller, restClient, 190);
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
        long delta = timeMe(start);

        return successCallResponse("Srvc Get Unlocked '", entity, delta, slept);
    }
}
