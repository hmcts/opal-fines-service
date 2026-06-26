package uk.gov.hmcts.opal.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import static uk.gov.hmcts.opal.controllers.util.ReportInstanceContentTestData.storedReportBytes;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import jakarta.servlet.ServletException;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.common.exceptions.standard.UnauthorizedException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_into_report_instance_content_cash_till_data.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_report_instance_content_cash_till_data.sql",
    executionPhase = AFTER_TEST_METHOD)
class ReportInstancesApiControllerGetContentIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/report-instances";
    private static final Long REPORT_INSTANCE_ID = 99000000353000L;
    private static final short BUSINESS_UNIT_ID = 1778;
    private static final String LOCATION = "stored-cash-till-report-location";

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Value("${opal.report.storage.container}")
    private String containerName;

    private BlobContainerClient blobContainerClient;

    @BeforeEach
    void setUp() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, SEARCH_AND_VIEW_ACCOUNTS);

        blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }
        blobContainerClient.getBlobClient(LOCATION)
            .upload(new ByteArrayInputStream(storedReportBytes), storedReportBytes.length, true);
    }

    @AfterEach
    void tearDown() {
        if (blobContainerClient != null && blobContainerClient.exists()) {
            blobContainerClient.getBlobClient(LOCATION).deleteIfExists();
        }
    }

    @Nested
    class GetReportInstanceContentHappyPath {

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenJsonRequested_returnsStoredReportContent_happyPath() throws Exception {
            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
                .andExpectAll(
                    status().isOk(),
                    content().contentTypeCompatibleWith(APPLICATION_JSON),
                    jsonPath("$.reportData.rows[0].cash_till_number").value("9011"),
                    jsonPath("$.reportData.rows[0].payment_method").value("NC")
                );
        }

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenCsvRequested_returnsBinaryContent_happyPath() throws Exception {
            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept("application/csv"))
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/csv"),
                    content().string(
                        "Business Unit,Cash Till Number,Cashier,Date,Type,Details,Payment Type,Amount,Receipt,"
                            + "Balance\n"
                            + "Cash Till Business Unit,9011,opal-test,26/05/2026,FA,ACC456,NC,125.50,R,124.50\n"
                    )
                );
        }
    }

    @Nested
    class GetReportInstanceContentSadPath {

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenNoTokenPresent_unauthorizedIsReturned_sadPath() {
            ServletException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ServletException.class,
                () -> mockMvc.perform(get(URL_BASE + "/" + REPORT_INSTANCE_ID + "/content").accept(APPLICATION_JSON))
            );

            org.assertj.core.api.Assertions.assertThat(exception.getCause())
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Current user is not authenticated with OpalJwtAuthenticationToken");
        }

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenRequestNotAcceptable_notAcceptableIsReturned_sadPath() throws Exception {
            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept("text/plain"))
                .andExpect(status().isNotAcceptable());
        }

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenUserLacksPermission_forbiddenIsReturned_sadPath() throws Exception {
            userStateStub.setupWithNoPermissions();

            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
                .andExpectAll(
                    status().isForbidden(),
                    jsonPath("$.title").value("Forbidden"),
                    jsonPath("$.detail").value("You do not have permission to access this resource"),
                    jsonPath("$.status").value(403),
                    jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"),
                    jsonPath("$.retriable").value(false)
                );
        }

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenStoredContentMissing_notFoundIsReturned_sadPath() throws Exception {
            jdbcTemplate.update("UPDATE report_instances SET location = NULL WHERE report_instance_id = ?",
                REPORT_INSTANCE_ID);

            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
                .andExpectAll(
                    status().isNotFound(),
                    jsonPath("$.title").value("Entity Not Found"),
                    jsonPath("$.detail").value("The requested entity could not be found"),
                    jsonPath("$.status").value(404)
                );
        }
    }

    private MockHttpServletRequestBuilder authorisedGetContent(Long reportInstanceId) {
        return get(URL_BASE + "/" + reportInstanceId + "/content")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken());
    }
}
