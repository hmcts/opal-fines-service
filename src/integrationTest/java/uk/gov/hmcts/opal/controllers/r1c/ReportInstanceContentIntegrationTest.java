package uk.gov.hmcts.opal.controllers.r1c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import static uk.gov.hmcts.opal.controllers.shared.util.ReportInstanceContentTestData.storedReportBytes;
import static uk.gov.hmcts.opal.testutil.JsonErrorAssertions.expectEntityNotFoundWithoutType;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.ReportInstanceFileEntity;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;
import uk.gov.hmcts.opal.repository.ReportInstanceFileRepository;
import uk.gov.hmcts.opal.util.UuidProvider;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = "classpath:db/insertData/insert_into_report_instance_content_cash_till_data.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_report_instance_content_cash_till_data.sql",
    executionPhase = AFTER_TEST_METHOD)
class ReportInstanceContentIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/report-instances";
    private static final Long REPORT_INSTANCE_ID = 99000000353000L;
    private static final short BUSINESS_UNIT_ID = 1778;
    private static final String LOCATION = "00000000-0000-0000-0000-000000353000";
    private static final UUID GENERATED_CSV_LOCATION =
        UUID.fromString("00000000-0000-0000-0000-000000353001");
    private static final UUID EXISTING_CSV_LOCATION =
        UUID.fromString("00000000-0000-0000-0000-000000353002");
    private static final UUID EXISTING_PDF_LOCATION =
        UUID.fromString("00000000-0000-0000-0000-000000353003");
    private static final String EXPECTED_CSV_CONTENT =
        "Business Unit,Cash Till Number,Cashier,Date,Type,Details,Payment Type,Amount,Receipt,"
            + "Balance\n"
            + "Cash Till Business Unit,9011,opal-test,26/05/2026,FA,ACC456,NC,125.50,R,124.50\n";

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Autowired
    private ReportInstanceFileRepository reportInstanceFileRepository;

    @MockitoBean
    private UuidProvider uuidProvider;

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
            blobContainerClient.getBlobClient(GENERATED_CSV_LOCATION.toString()).deleteIfExists();
            blobContainerClient.getBlobClient(EXISTING_CSV_LOCATION.toString()).deleteIfExists();
            blobContainerClient.getBlobClient(EXISTING_PDF_LOCATION.toString()).deleteIfExists();
        }
    }

    @Nested
    class GetReportInstanceContentHappyPath {

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-9503")
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
        @JiraTestKey("PO-9504")
        void whenCsvRequested_returnsBinaryContent_happyPath() throws Exception {
            when(uuidProvider.getUuid()).thenReturn(GENERATED_CSV_LOCATION);

            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept("application/csv"))
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/csv"),
                    content().string(EXPECTED_CSV_CONTENT)
                );
        }

        @Test
        void whenCsvRequestedAndNoRenderedFileExists_generatesStoresAndReturnsReport() throws Exception {
            when(uuidProvider.getUuid()).thenReturn(GENERATED_CSV_LOCATION);
            long blobCountBefore = blobCount();

            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept("application/csv"))
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/csv"),
                    content().string(EXPECTED_CSV_CONTENT)
                );

            Optional<ReportInstanceFileEntity> savedFile = reportInstanceFileRepository
                .findByReportInstanceIdAndFileType(REPORT_INSTANCE_ID, SupportedFileType.CSV);

            assertThat(savedFile).isPresent();
            assertThat(savedFile.orElseThrow().getLocationUuid()).isEqualTo(GENERATED_CSV_LOCATION);
            assertThat(blobContainerClient.getBlobClient(GENERATED_CSV_LOCATION.toString()).exists()).isTrue();
            assertThat(readBlob(GENERATED_CSV_LOCATION)).isEqualTo(EXPECTED_CSV_CONTENT);
            assertThat(blobCount()).isEqualTo(blobCountBefore + 1);
        }

        @Test
        void whenCsvRequestedAndRenderedFileAlreadyExists_returnsExistingReportWithoutRegenerating() throws Exception {
            String existingCsvContent = "existing,csv\nfrom,blob\n";
            uploadBlob(EXISTING_CSV_LOCATION, existingCsvContent);
            saveReportInstanceFile(SupportedFileType.CSV, EXISTING_CSV_LOCATION);
            long fileCountBefore = reportInstanceFileRepository.count();
            long blobCountBefore = blobCount();

            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept("application/csv"))
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/csv"),
                    content().string(existingCsvContent)
                );

            assertThat(reportInstanceFileRepository.count()).isEqualTo(fileCountBefore);
            assertThat(blobCount()).isEqualTo(blobCountBefore);
            verify(uuidProvider, never()).getUuid();
        }

        @Test
        void whenCsvRequestedAndOnlyPdfFileExists_generatesStoresAndReturnsCsvReport() throws Exception {
            uploadBlob(EXISTING_PDF_LOCATION, "existing pdf content");
            saveReportInstanceFile(SupportedFileType.PDF, EXISTING_PDF_LOCATION);
            when(uuidProvider.getUuid()).thenReturn(GENERATED_CSV_LOCATION);
            long blobCountBefore = blobCount();

            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept("application/csv"))
                .andExpectAll(
                    status().isOk(),
                    content().contentType("application/csv"),
                    content().string(EXPECTED_CSV_CONTENT)
                );

            Optional<ReportInstanceFileEntity> savedCsvFile = reportInstanceFileRepository
                .findByReportInstanceIdAndFileType(REPORT_INSTANCE_ID, SupportedFileType.CSV);

            assertThat(savedCsvFile).isPresent();
            assertThat(savedCsvFile.orElseThrow().getLocationUuid()).isEqualTo(GENERATED_CSV_LOCATION);
            assertThat(reportInstanceFileRepository
                .findByReportInstanceIdAndFileType(REPORT_INSTANCE_ID, SupportedFileType.PDF)).isPresent();
            assertThat(readBlob(GENERATED_CSV_LOCATION)).isEqualTo(EXPECTED_CSV_CONTENT);
            assertThat(blobCount()).isEqualTo(blobCountBefore + 1);
        }

        @Test
        void whenJsonRequested_doesNotStoreRenderedFileOrBlob() throws Exception {
            long fileCountBefore = reportInstanceFileRepository.count();
            long blobCountBefore = blobCount();

            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
                .andExpectAll(
                    status().isOk(),
                    content().contentTypeCompatibleWith(APPLICATION_JSON),
                    jsonPath("$.reportData.rows[0].cash_till_number").value("9011")
                );

            assertThat(reportInstanceFileRepository.count()).isEqualTo(fileCountBefore);
            assertThat(blobCount()).isEqualTo(blobCountBefore);
            verify(uuidProvider, never()).getUuid();
        }
    }

    @Nested
    class GetReportInstanceContentSadPath {

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-9499")
        void whenNoTokenPresent_unauthorizedIsReturned_sadPath() {
            org.assertj.core.api.Assertions.assertThatCode(
                () -> mockMvc.perform(get(URL_BASE + "/" + REPORT_INSTANCE_ID + "/content").accept(APPLICATION_JSON))
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
        @JiraTestKey("PO-9500")
        void whenRequestNotAcceptable_notAcceptableIsReturned_sadPath() throws Exception {
            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept("text/plain"))
                .andExpect(status().isNotAcceptable());
        }

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-9502")
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
        @JiraTestKey("PO-9501")
        void whenUserLacksPermissionInBusinessUnit_forbiddenIsReturned_sadPath() throws Exception {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 99, SEARCH_AND_VIEW_ACCOUNTS);

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
        @Sql(scripts = "classpath:db/insertData/set_cash_till_supported_types_csv_pdf.sql",
            executionPhase = BEFORE_TEST_METHOD)
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-9497")
        void whenRequestedContentTypeUnsupported_unprocessableContentIsReturned_sadPath() throws Exception {
            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
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
        @JiraTestKey("PO-9498")
        void whenStoredContentMissing_internalServerErrorIsReturned_sadPath() throws Exception {
            blobContainerClient.getBlobClient(LOCATION).deleteIfExists();

            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept(APPLICATION_JSON))
                .andExpectAll(
                    status().isInternalServerError(),
                    content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON),
                    jsonPath("$.title").value("Missing Data In Storage Account"),
                    jsonPath("$.detail").value(
                        "Stored report content file '00000000-0000-0000-0000-000000353000' was not found for "
                            + "report instance id: 99000000353000"
                    ),
                    jsonPath("$.status").value(500),
                    jsonPath("$.retriable").value(false)
                );
        }

        @Test
        @JiraStory("PO-2253")
        @JiraEpic("PO-2248")
        @JiraTestKey("PO-9496")
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
        @JiraTestKey("PO-9495")
        void whenReportServiceMissing_internalServerErrorIsReturned_sadPath() throws Exception {
            mockMvc.perform(authorisedGetContent(REPORT_INSTANCE_ID).accept("application/csv"))
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

    private void saveReportInstanceFile(SupportedFileType fileType, UUID locationUuid) {
        reportInstanceFileRepository.save(ReportInstanceFileEntity.builder()
            .reportInstanceId(REPORT_INSTANCE_ID)
            .fileType(fileType)
            .locationUuid(locationUuid)
            .createdTimestamp(LocalDateTime.parse("2026-05-27T09:30:00"))
            .lastAccessedTimestamp(LocalDateTime.parse("2026-05-27T09:30:00"))
            .build());
    }

    private void uploadBlob(UUID location, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        blobContainerClient.getBlobClient(location.toString())
            .upload(new ByteArrayInputStream(bytes), bytes.length, true);
    }

    private String readBlob(UUID location) {
        return new String(blobContainerClient.getBlobClient(location.toString())
            .downloadContent()
            .toBytes(), StandardCharsets.UTF_8);
    }

    private long blobCount() {
        return blobContainerClient.listBlobs().stream().count();
    }
}
