package uk.gov.hmcts.opal;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/opalMode")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@Opal and not @Smoke and not @Ignore")
public class OpalTestRunner {

}
