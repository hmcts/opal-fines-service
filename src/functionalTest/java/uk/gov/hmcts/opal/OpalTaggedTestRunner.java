package uk.gov.hmcts.opal;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;

/**
 * Runs tagged Opal functional scenarios that must be selected deliberately at execution time.
 *
 * <p>This runner exists separately from {@link OpalTestRunner} because the default Opal suite
 * excludes feature-toggle scenarios tagged {@code @FeatureToggle}. Those scenarios are not safe
 * to include in the normal {@code functionalOpal} run because they depend on the target environment
 * being configured with a matching feature-flag combination.
 *
 * <p>The accompanying {@code functionalOpalTags} Gradle task supplies
 * {@code cucumber.filter.tags}, allowing a caller to run only the relevant tagged scenarios
 * against an environment prepared for that configuration.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/opalMode")
public class OpalTaggedTestRunner {

    static Logger log = LoggerFactory.getLogger(OpalTaggedTestRunner.class.getName());

    static {
        log.info("FINES URL: {}", BaseStepDef.getTestUrl());
        log.info("LOGGING URL: {}", BaseStepDef.getLoggingTestUrl());
    }
}
