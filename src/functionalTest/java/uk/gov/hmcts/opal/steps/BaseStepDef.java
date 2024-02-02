package uk.gov.hmcts.opal.steps;

public class BaseStepDef {

    private static final String TEST_URL = System.getenv().getOrDefault("TEST_URL", "http://localhost:4550");
    protected static String getTestUrl() {
        return TEST_URL;
    }
}
