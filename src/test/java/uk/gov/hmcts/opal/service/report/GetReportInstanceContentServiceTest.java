package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.report.FileType.CSV;
import static uk.gov.hmcts.opal.service.report.FileType.JSON;
import static uk.gov.hmcts.opal.service.report.GetReportInstanceContentTestData.createReportInstanceEntity;
import static uk.gov.hmcts.opal.service.report.GetReportInstanceContentTestData.createStoredReportContent;
import static uk.gov.hmcts.opal.service.report.GetReportInstanceContentTestData.createTestReportData;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.gov.hmcts.opal.service.report.GetReportInstanceContentTestData.TestReportData;

@ExtendWith(MockitoExtension.class)
class GetReportInstanceContentServiceTest {

    private static final String LOCATION = "location";
    private static final String REPORT_ID = "report-id";
    private static final String REPORT_JSON = "{\"report_data\":{\"rows\":2}}";

    @Mock
    private ReportInstanceRepository reportInstanceRepository;

    @Mock
    private ReportRegistry reportRegistry;

    @Mock
    private ReportBlobStore reportBlobStore;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ReportInterface<ReportDataInterface> reportInterfaceImplementation;

    @Mock
    private OpalJwtAuthenticationToken authToken;

    private GetReportInstanceContentService getReportInstanceContentService;
    private ReportInstanceEntity reportInstance;
    private TestReportData reportData;
    private StoredReportContent storedReportContent;

    @BeforeEach
    void setUp() {
        getReportInstanceContentService = new GetReportInstanceContentService(
            reportInstanceRepository,
            reportRegistry,
            reportBlobStore,
            mapper
        );
        mock_authenticationContext();
        reportInstance = createReportInstanceEntity(
            REPORT_ID,
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS,
            List.of((short) 77)
        );
        reportData = createTestReportData();
        storedReportContent = createStoredReportContent(Map.of("rows", 2));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class GetReportInstanceContent {

        @Test
        void whenReportInstanceMissing_throwsEntityNotFound_sadPath() {
            when(reportInstanceRepository.findById(1L)).thenReturn(Optional.empty());

            assertAll(
                () -> assertThatThrownBy(() -> getReportInstanceContentService.getReportInstanceContent(1L, JSON))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Report instance not found for id: 1"),
                () -> verifyNoInteractions(reportBlobStore)
            );
        }

        @Test
        void whenJsonRequested_returnsStoredContent_happyPath() throws JacksonException {
            mock_reportInstanceAtLocation(LOCATION);
            mock_storedReportContent(REPORT_JSON);
            mock_hasPermissionForReportAndBusinessUnit();

            Map<String, Object> expected = Map.of("report_data", Map.of("rows", 2));
            mock_jsonStoredReport(expected);

            Object actual = getReportInstanceContentService.getReportInstanceContent(1L, JSON);

            assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(reportBlobStore).getReport(LOCATION)
            );
        }

        @Test
        void whenJsonRequestedAndLocationIsMissing_throwsEntityNotFound_sadPath() {
            mock_reportInstanceAtLocation(" ");
            mock_hasPermissionForReportAndBusinessUnit();

            assert_reportInstanceContentNotFound(JSON);
        }

        @Test
        void whenJsonRequestedAndBlobContentIsMissing_throwsEntityNotFound_sadPath() {
            mock_reportInstanceAtLocation(LOCATION);
            mock_hasPermissionForReportAndBusinessUnit();

            assertAll(
                () -> assert_reportInstanceContentNotFound(JSON),
                () -> verify(reportBlobStore).getReport(LOCATION)
            );
        }

        @Test
        void whenJsonRequestedAndStoredContentIsInvalid_throwsIllegalStateException_sadPath()
            throws JacksonException {
            mock_reportInstanceAtLocation(LOCATION);
            mock_storedReportContent("not-json");
            mock_hasPermissionForReportAndBusinessUnit();
            JacksonException parseException = new JacksonException("bad json") {
            };
            when(mapper.readValue(eq("not-json"), typeReferenceAny())).thenThrow(parseException);

            assertThatThrownBy(() -> getReportInstanceContentService.getReportInstanceContent(1L, JSON))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Stored report content is not valid JSON for id: 1")
                .hasCause(parseException);
        }

        @Test
        void whenCsvRequested_generatesFileContent_happyPath() {
            mock_reportInstanceAtLocation(LOCATION);
            mock_reportTemplateLookup();
            mock_storedReportContent(REPORT_JSON);
            mock_hasPermissionForReportAndBusinessUnit();
            when(mapper.readValue(REPORT_JSON, StoredReportContent.class))
                .thenReturn(storedReportContent);
            doReturn(GetReportInstanceContentTestData.TestReportData.class)
                .when(reportInterfaceImplementation).getStoredReportDataClass(reportInstance);
            when(mapper.convertValue(Map.of("rows", 2), GetReportInstanceContentTestData.TestReportData.class))
                .thenReturn(reportData);
            byte[] expected = "a,b".getBytes();
            when(reportInterfaceImplementation.convertReportDataToFileType(reportInstance, reportData, CSV))
                .thenReturn(expected);

            Object actual = getReportInstanceContentService.getReportInstanceContent(1L, CSV);

            assertAll(
                () -> assertArrayEquals(expected, (byte[]) actual),
                () -> verify(reportBlobStore).getReport(LOCATION),
                () -> verify(reportInterfaceImplementation, never()).generateReportData(reportInstance),
                () -> verify(reportInterfaceImplementation)
                    .convertReportDataToFileType(reportInstance, reportData, CSV)
            );
        }

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.opal.service.report.GetReportInstanceContentServiceTest#missingBusinessUnits")
        void whenBusinessUnitsMissing_throwsPermissionNotAllowedException_sadPath(List<Short> businessUnits) {
            reportInstance.setBusinessUnit(businessUnits);
            mock_reportInstanceAtLocation(LOCATION);
            when(authToken.hasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);

            assertAll(
                () -> assertThatThrownBy(() -> getReportInstanceContentService.getReportInstanceContent(1L, JSON))
                    .isInstanceOf(PermissionNotAllowedException.class),
                () -> verify(authToken, never())
                    .hasPermissionInBusinessUnit(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS, (short) 77)
            );
        }

        @Test
        void whenCsvRequestedAndLocationIsMissing_throwsEntityNotFound_sadPath() {
            when(reportInstanceRepository.findById(1L)).thenReturn(Optional.of(reportInstance));
            mock_hasPermissionForReportAndBusinessUnit();

            assert_reportInstanceContentNotFound(CSV);
        }

        @Test
        void whenCsvRequestedAndStoredContentIsInvalid_throwsIllegalStateException_sadPath() throws JacksonException {
            mock_reportTemplateLookup();
            reportInstance.setLocation(LOCATION);
            mock_storedReportContent("not-json");
            mock_hasPermissionForReportAndBusinessUnit();
            JacksonException parseException = new JacksonException("bad json") {
            };
            when(mapper.readValue("not-json", StoredReportContent.class)).thenThrow(parseException);

            assertThatThrownBy(() -> getReportInstanceContentService.getReportInstanceContent(1L, CSV))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Stored report content is not valid JSON for id: 1")
                .hasCause(parseException);
        }

        @Test
        void whenReportPermissionIsNull_throwsPermissionNotAllowedException() {
            reportInstance.getReport().setPermission(null);
            mock_reportInstanceAtLocation(LOCATION);

            assertAll(
                () -> assertThatThrownBy(() -> getReportInstanceContentService.getReportInstanceContent(1L, JSON))
                    .isInstanceOf(PermissionNotAllowedException.class),
                () -> verify(reportBlobStore, never()).getReport(any())
            );
        }

        @Test
        void whenUserLacksReportPermission_throwsPermissionNotAllowedException() {
            mock_reportInstanceAtLocation(LOCATION);

            assertAll(
                () -> assertThatThrownBy(() -> getReportInstanceContentService.getReportInstanceContent(1L, JSON))
                    .isInstanceOf(PermissionNotAllowedException.class),
                () -> verify(reportBlobStore, never()).getReport(any())
            );
        }

        @Test
        void whenUserLacksBusinessUnitPermission_throwsPermissionNotAllowedException() {
            mock_reportInstanceAtLocation(LOCATION);
            when(authToken.hasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
            when(authToken.hasPermissionInBusinessUnit(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS, (short) 77))
                .thenReturn(false);

            assertAll(
                () -> assertThatThrownBy(() -> getReportInstanceContentService.getReportInstanceContent(1L, JSON))
                    .isInstanceOf(PermissionNotAllowedException.class),
                () -> verify(reportBlobStore, never()).getReport(any())
            );
        }

    }

    private void mock_reportInstanceAtLocation(String location) {
        reportInstance.setLocation(location);
        when(reportInstanceRepository.findById(1L)).thenReturn(Optional.of(reportInstance));
    }

    private void mock_authenticationContext() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mock_storedReportContent(String reportContent) {
        when(reportBlobStore.getReport(LOCATION)).thenReturn(reportContent);
    }

    private void mock_reportTemplateLookup() {
        when(reportInstanceRepository.findById(1L)).thenReturn(Optional.of(reportInstance));
        doReturn(reportInterfaceImplementation).when(reportRegistry).get(REPORT_ID);
    }

    private void mock_hasPermissionForReportAndBusinessUnit() {
        when(authToken.hasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);
        when(authToken.hasPermissionInBusinessUnit(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS, (short) 77))
            .thenReturn(true);
    }

    private void mock_jsonStoredReport(Map<String, Object> expected) throws JacksonException {
        when(mapper.readValue(eq(REPORT_JSON), typeReferenceAny())).thenReturn(expected);
    }

    @SuppressWarnings("unchecked")
    private TypeReference<Map<String, Object>> typeReferenceAny() {
        return any(TypeReference.class);
    }

    private void assert_reportInstanceContentNotFound(FileType fileType) {
        assertThatThrownBy(() -> getReportInstanceContentService.getReportInstanceContent(1L, fileType))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Report instance content not found for id: 1");
    }

    private static Stream<Arguments> missingBusinessUnits() {
        return Stream.of(
            Arguments.of((Object) null),
            Arguments.of(List.of())
        );
    }
}
