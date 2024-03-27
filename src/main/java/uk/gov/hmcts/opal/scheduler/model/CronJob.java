package uk.gov.hmcts.opal.scheduler.model;

import org.quartz.Job;

import java.util.Collections;
import java.util.Map;

public interface CronJob extends Job {

    String getCronExpression();

    default Map<String, Object> getData() {
        return Collections.emptyMap();
    }
}
