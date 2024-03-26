package uk.gov.hmcts.opal.scheduler.model;

import org.quartz.Job;

public interface CronJob extends Job {

    String getCronExpression();
}
