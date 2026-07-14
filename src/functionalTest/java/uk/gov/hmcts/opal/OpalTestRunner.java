package uk.gov.hmcts.opal;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/opalMode")
@ConfigurationParameter(
    key = FILTER_TAGS_PROPERTY_NAME,
    value = "@Opal and not @Smoke and not @Ignore and not "
        + "(@R1AOff or @R1BOff or @R1COff or @R1CWriteOffOff or @R1CWriteOff or "
        + "@R1CEnforcementOperationalReportingOff or @R1CAdministrationOff or @R1CFinancialMovementsOff)"
)
public class OpalTestRunner {

    static Logger log = LoggerFactory.getLogger(OpalTestRunner.class.getName());

    static {
        log.info("FINES URL: {}", BaseStepDef.getTestUrl());
        log.info("LOGGING URL: {}", BaseStepDef.getLoggingTestUrl());
    }

}
