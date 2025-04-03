package uk.gov.hmcts.opal.zephyr;

import io.cucumber.java.Scenario;
import io.cucumber.java.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.zephyr.model.ExecutionUpdateVO;
import uk.gov.hmcts.opal.zephyr.model.ExecutionVO;
import uk.gov.hmcts.opal.zephyr.model.JiraIssueVO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static uk.gov.hmcts.opal.zephyr.JIRAConstants.*;

public class ZephyrRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZephyrRunner.class);

    public static void updateJIRA(Scenario scenario) {
        LOGGER.info("Update JIRA :: Start");
        //tempPrint(scenario);
        try {
            for (String tagName: scenario.getSourceTagNames()) {
                String testIssueId = getIssueId(tagName);
                if ( testIssueId != null) {
                    String executionIssueId = createExecution(testIssueId, scenario);
                    updateExecution(executionIssueId, scenario);
                }
            }
        } finally {
            LOGGER.info("Update JIRA :: Done");
        }
    }

    private static void tempPrint(Scenario scenario) {
        JIRARestClient.getLoggedInUser();
    }

    private static String createExecution(String issueId, Scenario scenario) {
        ExecutionVO executionVO = new ExecutionVO();
        executionVO.setIssueId(issueId);
        executionVO.setCycleId(TEST_CYCLE_ID);
        executionVO.setAssignee(TEST_ASSIGNE);
        executionVO.setAssigneeType(TEST_ASSIGNEETYPE);
        executionVO.setFolderId(TEST_FOLDERID);
        executionVO.setProjectId(TEST_PROJECT_ID);
        executionVO.setVersionId(TEST_VERSION_ID);
        return JIRARestClient.createExecution(executionVO);
    }

    private static void updateExecution(String executionIssueId, Scenario scenario) {
        String status = (scenario.isFailed()) ? "2" : "1";
        ExecutionUpdateVO executionUpdateVO = new ExecutionUpdateVO();
        executionUpdateVO.setStatus(status);
        executionUpdateVO.setComment("Functional :: Automation Testing Execution ["+getTimeStamp()+"]");
        JIRARestClient.updateExecution(executionIssueId, executionUpdateVO);
    }

    private static String getIssueId(String tagName) {
        LOGGER.info("Processing [{}]", tagName);
        try {
            if (tagName.startsWith("@PO-")) {
                String issueId = JIRARestClient.getIssueDetails(tagName.replaceAll("@", ""));
                LOGGER.info("IssueId from JIRA : {} ", issueId);
                return issueId;
            }
        } catch (Exception e) {
            LOGGER.error("Exception with get Issue Details : [{}]", e.getMessage());
        }
        return null;
    }
    private static String getTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}
