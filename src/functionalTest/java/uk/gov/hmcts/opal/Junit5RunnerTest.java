package uk.gov.hmcts.opal;

import io.cucumber.core.options.Constants;
import io.cucumber.java.*;
import org.junit.AfterClass;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.zephyr.ZephyrRunner;

import java.util.ArrayList;
import java.util.List;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")

@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME,value = "@PO-1169")

public class Junit5RunnerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(Junit5RunnerTest.class);

    private static List<Scenario> scenarioList = new ArrayList<>();

    @BeforeAll
    public static void beforeAllScenarios() {
        //TODO - Need to enhance to include code for JIRA-Folder in JIRA Test Cycles.
        LOGGER.info("BeforeAllScenarios");
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        scenarioList.add(scenario);
    }

    @After
    public static void afterScenario(Scenario scenario) {
        ZephyrRunner.updateJIRA(scenario);
    }

    @AfterAll
    public static void afterAll() {
        LOGGER.info("ScenarioList : {}", scenarioList);
        for (Scenario scenario : scenarioList){
            LOGGER.info("{} --- {}, [{}] = {}",scenario.getId(), scenario.getName(),scenario.getSourceTagNames(), scenario.getStatus());
        }
    }

    @AfterClass
    public static void afterClass() {
        LOGGER.info("\n\n\n\nAfterClass\n\n\n\n");
    }
}
