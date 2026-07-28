package uk.gov.hmcts.opal.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import static uk.gov.hmcts.opal.controllers.util.ReportInstanceContentTestData.cashListStoredReportBytes;
import static uk.gov.hmcts.opal.controllers.util.ReportInstanceContentTestData.cashTillStoredReportBytes;
import static uk.gov.hmcts.opal.testutil.JsonErrorAssertions.expectEntityNotFoundWithoutType;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = "classpath:db/insertData/insert_into_report_instance_content_cash_till_data.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_report_instance_content_cash_till_data.sql",
    executionPhase = AFTER_TEST_METHOD)
class ReportInstancesApiControllerGetContentIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/report-instances";
    private static final Long CASH_TILL_REPORT_INSTANCE_ID = 99000000353000L;
    private static final Long CASH_LIST_REPORT_INSTANCE_ID = 99000000343000L;
    private static final short CASH_TILL_BUSINESS_UNIT_ID = 1778;
    private static final short CASH_LIST_BUSINESS_UNIT_ID = 1777;
    private static final String CASH_TILL_LOCATION = "stored-cash-till-report-location";
    private static final String CASH_LIST_LOCATION = "stored-cash-list-report-location";

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Value("${opal.report.storage.container}")
    private String containerName;

    private BlobContainerClient blobContainerClient;

    @BeforeEach
    void setUp() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(CASH_TILL_BUSINESS_UNIT_ID, SEARCH_AND_VIEW_ACCOUNTS);
        userStateStub.addPermissions(CASH_LIST_BUSINESS_UNIT_ID, SEARCH_AND_VIEW_ACCOUNTS);

        blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }
        blobContainerClient.getBlobClient(CASH_TILL_LOCATION)
            .upload(new ByteArrayInputStream(cashTillStoredReportBytes), cashTillStoredReportBytes.length, true);
        blobContainerClient.getBlobClient(CASH_LIST_LOCATION)
            .upload(new ByteArrayInputStream(cashListStoredReportBytes), cashListStoredReportBytes.length, true);
    }

    @AfterEach
    void tearDown() {
        if (blobContainerClient != null && blobContainerClient.exists()) {
            blobContainerClient.getBlobClient(CASH_TILL_LOCATION).deleteIfExists();
            blobContainerClient.getBlobClient(CASH_LIST_LOCATION).deleteIfExists();
        }
    }

    @Nested
    class GetReportInstanceContentHappyPath {

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenJsonRequested_returnsStoredReportContent_happyPath() throws Exception {
            mockMvc.perform(authorisedGetContent(CASH_TILL_REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
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
            mockMvc.perform(authorisedGetContent(CASH_TILL_REPORT_INSTANCE_ID).accept("application/csv"))
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
    class GetCashListReportInstanceContentHappyPath {

        @Test
        @JiraStory("PO-3470")
        @JiraEpic("PO-2116")
        void whenJsonRequested_returnsStoredCashListContent_happyPath() throws Exception {
            mockMvc.perform(authorisedGetContent(CASH_LIST_REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
                .andExpectAll(
                    status().isOk(),
                    content().contentTypeCompatibleWith(APPLICATION_JSON),
                    jsonPath("$.reportData.entries[0].accountNumber").value("ACC123"),
                    jsonPath("$.reportData.entries[1].nameAdditionalInformation")
                        .value("Auto - Suspense payment"),
                    jsonPath("$.reportData.total").value(165.50)
                );
        }

        @Test
        @JiraStory("PO-3470")
        @JiraEpic("PO-2116")
        void whenCsvRequested_returnsCashListCsv_happyPath() throws Exception {
            mockMvc.perform(authorisedGetContent(CASH_LIST_REPORT_INSTANCE_ID).accept("application/csv"))
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/csv"),
                    content().string(
                        "Entry,Type,Susp.,Account no,Name,Name (Additional Information),Payment,Amount\n"
                            + "1,FA,,ACC123,DOE Jane,,NC,125.50\n"
                            + "2,SA,UN,Suspense Ref,1,Auto - Suspense payment,CT,40.00\n"
                            + "Total\n"
                            + "165.50\n"
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
            org.assertj.core.api.Assertions.assertThatCode(
                () -> mockMvc.perform(get(URL_BASE + "/" + CASH_TILL_REPORT_INSTANCE_ID + "/content")
                        .accept(APPLICATION_JSON))
                    .andExpectAll(
                        status().isUnauthorized(),
                        content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON),
                        jsonPath("$.title").value("Unauthorized"),
                        jsonPath("$.detail").value("Missing or invalid access token"),
                        jsonPath("$.status").value(401),
                        jsonPath("$.retriable").value(false)
                    )
            ).doesNotThrowAnyException();
        }

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenRequestNotAcceptable_notAcceptableIsReturned_sadPath() throws Exception {
            mockMvc.perform(authorisedGetContent(CASH_TILL_REPORT_INSTANCE_ID).accept("text/plain"))
                .andExpect(status().isNotAcceptable());
        }

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenUserLacksPermission_forbiddenIsReturned_sadPath() throws Exception {
            userStateStub.setupWithNoPermissions();

            mockMvc.perform(authorisedGetContent(CASH_TILL_REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
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
        void whenUserLacksPermissionInBusinessUnit_forbiddenIsReturned_sadPath() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 99, SEARCH_AND_VIEW_ACCOUNTS);

            mockMvc.perform(authorisedGetContent(CASH_TILL_REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
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
        @Sql(scripts = "classpath:db/insertData/set_cash_till_supported_types_csv_pdf.sql",
            executionPhase = BEFORE_TEST_METHOD)
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenRequestedContentTypeUnsupported_unprocessableContentIsReturned_sadPath() throws Exception {
            mockMvc.perform(authorisedGetContent(CASH_TILL_REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
                .andExpectAll(
                    status().isUnprocessableContent(),
                    content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON),
                    jsonPath("$.title").value("Report Content Type Not Supported"),
                    jsonPath("$.detail").value(
                        "Content type JSON is not supported for report 'cash_till'. "
                            + "Supported content types: CSV, PDF"
                    ),
                    jsonPath("$.status").value(422),
                    jsonPath("$.retriable").value(false)
                );
        }

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenStoredContentMissing_internalServerErrorIsReturned_sadPath() throws Exception {
            blobContainerClient.getBlobClient(CASH_TILL_LOCATION).deleteIfExists();

            mockMvc.perform(authorisedGetContent(CASH_TILL_REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
                .andExpectAll(
                    status().isInternalServerError(),
                    content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON),
                    jsonPath("$.title").value("Missing Data In Storage Account"),
                    jsonPath("$.detail").value(
                        "Stored report content file 'stored-cash-till-report-location' was not found for "
                            + "report instance id: 99000000353000"
                    ),
                    jsonPath("$.status").value(500),
                    jsonPath("$.retriable").value(false)
                );
        }

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenReportInstanceMissing_notFoundIsReturned_sadPath() throws Exception {
            mockMvc.perform(authorisedGetContent(99999999999999L).accept(APPLICATION_JSON))
                .andExpectAll(
                    status().isNotFound(),
                    content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON),
                    expectEntityNotFoundWithoutType(),
                    jsonPath("$.retriable").value(false)
                );
        }

        @Test
        @Sql(scripts = "classpath:db/insertData/insert_missing_report_service_content_data.sql",
            executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:db/deleteData/delete_missing_report_service_content_data.sql",
            executionPhase = AFTER_TEST_METHOD)
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        void whenReportServiceMissing_internalServerErrorIsReturned_sadPath() throws Exception {
            mockMvc.perform(authorisedGetContent(CASH_TILL_REPORT_INSTANCE_ID).accept("application/csv"))
                .andExpectAll(
                    status().isInternalServerError(),
                    content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON),
                    jsonPath("$.title").value("Missing Report Service"),
                    jsonPath("$.detail").value(
                        "No report service implementation found for reportId: missing_service_report"
                    ),
                    jsonPath("$.status").value(500),
                    jsonPath("$.retriable").value(false)
                );
        }
    }

    private MockHttpServletRequestBuilder authorisedGetContent(Long reportInstanceId) {
        return get(URL_BASE + "/" + reportInstanceId + "/content")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken());
    }
}
