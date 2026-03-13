package uk.gov.hmcts.opal.util;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidProvider {
    public UUID getUuid() {
        return UUID.randomUUID();
    }

}
