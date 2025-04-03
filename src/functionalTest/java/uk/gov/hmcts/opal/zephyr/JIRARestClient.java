package uk.gov.hmcts.opal.zephyr;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.opal.zephyr.model.BaseJiraVO;
import uk.gov.hmcts.opal.zephyr.model.ExecutionUpdateVO;
import uk.gov.hmcts.opal.zephyr.model.ExecutionVO;
import uk.gov.hmcts.opal.zephyr.model.JiraIssueVO;

import static uk.gov.hmcts.opal.zephyr.JIRAConstants.*;

public class JIRARestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JIRARestClient.class);

    public static String getIssueDetails(String jiraIssueKey) {
        try {
            HttpEntity<HttpHeaders> httpEntity = getHttpEntity();

            LOGGER.info("Issue Key URL : {} ", prepareIssueDetailsURL(jiraIssueKey));
            ResponseEntity<JiraIssueVO> result = new RestTemplate().exchange(prepareIssueDetailsURL(jiraIssueKey), HttpMethod.GET, httpEntity, JiraIssueVO.class);
            LOGGER.info("{} -----> Issue Details Code: {}", jiraIssueKey, result.getStatusCode());
            if(result.getStatusCode().is2xxSuccessful()) {
                LOGGER.info("Issue Details : {}", result.getBody());
                JiraIssueVO issueVO = result.getBody();
                LOGGER.info("Id : [{}], IssueType : [{}]", issueVO.getId(), issueVO.getFields().getIssuetype().getName());
                if("Test".equals(issueVO.getFields().getIssuetype().getName())) {
                    return issueVO.getId();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Not Test Type [{}] Error : {}", jiraIssueKey, e.getMessage());
        }
        return null;
    }

    public static String createExecution(ExecutionVO executionVO) {
        try {
            HttpEntity<BaseJiraVO> httpEntity = getHttpEntity(executionVO);

            LOGGER.info("Create Execution URL : {} ", prepareCreateExecutionURL());
            ResponseEntity<String> result = new RestTemplate().exchange(prepareCreateExecutionURL(), HttpMethod.POST, httpEntity, String.class);
            LOGGER.info("{} -----> Create Execution Details : {}", executionVO.getIssueId(), result.getStatusCode());
            if(result.getStatusCode().is2xxSuccessful()) {
                LOGGER.info("Create Execution Response : {}", getBody(result));
                JSONArray jsonArray = JsonPath.parse(getBody(result)).read("$..id");
                String executionId = jsonArray.getFirst().toString();
                LOGGER.info("ExecutionId : {}", executionId);
                return executionId;
            }
        } catch (Exception e) {
            LOGGER.warn("Create Execution failed [{}] Error : {}", executionVO.getIssueId(), e.getMessage());
        }
        return null;
    }

    private static @Nullable String getBody(ResponseEntity<String> result) {
        return result.getBody();
    }

    public static String updateExecution(String executionIssueId, ExecutionUpdateVO executionUpdateVO) {
        try {
            HttpEntity<BaseJiraVO> httpEntity = getHttpEntity(executionUpdateVO);

            LOGGER.info("Update Execution URL : {} ", prepareCreateExecutionURL());
            ResponseEntity<String> result = new RestTemplate().exchange(prepareUpdateExecutionURL(executionIssueId), HttpMethod.PUT, httpEntity, String.class);
            LOGGER.info("{} -----> Update Execution Details : {}", executionIssueId, result.getStatusCode());
            if(result.getStatusCode().is2xxSuccessful()) {
                LOGGER.info("Update Execution Response : {}", result.getBody());
            }
        } catch (Exception e) {
            LOGGER.warn("Update Execution failed [{}] Error : {}", executionIssueId, e.getMessage());
        }
        return null;
    }

    public static void getLoggedInUser() {
        HttpEntity<HttpHeaders> httpEntity = getHttpEntity();
        LOGGER.info("LoggedIn URL : {} ", getLoggedInURL());
        ResponseEntity<String> result = new RestTemplate().exchange(getLoggedInURL(), HttpMethod.GET, httpEntity, String.class);
        LOGGER.info("Response Entity : {}", result);
        LOGGER.info("Response Code: {}", result.getStatusCode());
        LOGGER.info("Response : {}", result.getBody());

    }

    private static HttpEntity<HttpHeaders> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, BEARER + JIRA_TOKEN);
        LOGGER.info("Token for GET : {} ", BEARER + JIRA_TOKEN);
        return new HttpEntity<>(headers);
    }
    private static HttpEntity<BaseJiraVO> getHttpEntity(BaseJiraVO jiraVO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, BEARER + JIRA_TOKEN);
        LOGGER.info("Token for POST/PUT : {} ", BEARER + JIRA_TOKEN);
        return new HttpEntity<>(jiraVO, headers);
    }

    private static String getLoggedInURL() {
        return URL_JIRA_BASE + PATH_JIRA_ZEPHYR + PATH_JIRA_ZQL_USER;
    }

    private static String prepareIssueDetailsURL(String jiraIssueKey) {
        return URL_JIRA_BASE + PATH_JIRA_REST_2 + PATH_JIRA_ISSUE + PATH_SEPARATOR + jiraIssueKey ;
    }

    private static String prepareCreateExecutionURL() {
        return URL_JIRA_BASE + PATH_JIRA_ZEPHYR + PATH_JIRA_EXECUTION;
    }

    private static String prepareUpdateExecutionURL(String issueId) {
        return prepareCreateExecutionURL() + PATH_SEPARATOR + issueId + PATH_JIRA_EXECUTE;
    }
}
