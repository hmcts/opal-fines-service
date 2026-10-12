package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
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
import uk.gov.hmcts.opal.entity.ReportInstanceFileEntity;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;
import uk.gov.hmcts.opal.exception.MissingStoredReportContentException;
import uk.gov.hmcts.opal.repository.ReportInstanceFileRepository;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.gov.hmcts.opal.service.report.GetReportInstanceContentTestData.TestReportData;

@ExtendWith(MockitoExtension.class)
class GetReportInstanceContentServiceTest {

    private static final UUID LOCATION = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID GENERATED_FILE_LOCATION = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID EXISTING_FILE_LOCATION = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final String REPORT_ID = "report-id";
    private static final String REPORT_JSON = "{\"report_data\":{\"rows\":2}}";

    @Mock
    private ReportInstanceRepository reportInstanceRepository;

    @Mock
    private ReportInstanceFileRepository reportInstanceFileRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-03T10:15:30Z"), ZoneOffset.UTC);

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
        mock_authenticationContext();
        reportInstance = createReportInstanceEntity(
            REPORT_ID,
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS,
            List.of((short) 77)
        );
        reportInstance.setReportInstanceId(1L);
        reportData = createTestReportData();
        storedReportContent = createStoredReportContent(Map.of("rows", 2));
        getReportInstanceContentService = new GetReportInstanceContentService(
            reportInstanceRepository,
            reportInstanceFileRepository,
            clock,
            reportRegistry,
            reportBlobStore,
            mapper
        );
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
            mock_reportInstanceAtLocation(null);
            mock_hasPermissionForReportAndBusinessUnit();

            assert_reportInstanceContentNotFound(JSON);
        }

        @Test
        void whenJsonRequestedAndBlobContentIsMissing_throwsMissingStoredReportContentException_sadPath() {
            mock_reportInstanceAtLocation(LOCATION);
            mock_hasPermissionForReportAndBusinessUnit();
            when(reportBlobStore.getReport(LOCATION))
                .thenThrow(new MissingStoredReportContentException(LOCATION));

            assertAll(
                () -> assertThatThrownBy(() -> getReportInstanceContentService.getReportInstanceContent(1L, JSON))
                    .isInstanceOf(MissingStoredReportContentException.class)
                    .hasMessage("Stored report content file '" + LOCATION
                        + "' was not found for report instance id: 1"),
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
        void whenCsvRequested_generatesFileContent_happyPath() throws Exception {
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
            when(reportInstanceFileRepository.findByReportInstanceIdAndFileType(1L, SupportedFileType.CSV))
                .thenReturn(Optional.empty());
            when(reportInterfaceImplementation.convertReportDataToFileType(reportInstance, reportData, CSV))
                .thenReturn(expected);
            when(reportBlobStore.storeReport(any())).thenReturn(GENERATED_FILE_LOCATION);

            Object actual = getReportInstanceContentService.getReportInstanceContent(1L, CSV);

            ArgumentCaptor<InputStream> storedFile = ArgumentCaptor.forClass(InputStream.class);
            ArgumentCaptor<ReportInstanceFileEntity> fileEntity =
                ArgumentCaptor.forClass(ReportInstanceFileEntity.class);

            assertAll(
                () -> assertArrayEquals(expected, (byte[]) actual),
                () -> verify(reportBlobStore).getReport(LOCATION),
                () -> verify(reportBlobStore).storeReport(storedFile.capture()),
                () -> assertArrayEquals(expected, storedFile.getValue().readAllBytes()),
                () -> verify(reportInstanceFileRepository).save(fileEntity.capture()),
                () -> assertEquals(1L, fileEntity.getValue().getReportInstanceId()),
                () -> assertEquals(SupportedFileType.CSV, fileEntity.getValue().getFileType()),
                () -> assertEquals(GENERATED_FILE_LOCATION, fileEntity.getValue().getLocationUuid()),
                () -> assertEquals(LocalDateTime.now(clock), fileEntity.getValue().getCreatedTimestamp()),
                () -> assertEquals(LocalDateTime.now(clock), fileEntity.getValue().getLastAccessedTimestamp()),
                () -> verify(reportInterfaceImplementation, never()).generateReportData(reportInstance),
                () -> verify(reportInterfaceImplementation)
                    .convertReportDataToFileType(reportInstance, reportData, CSV)
            );
        }

        @Test
        void whenCsvRequestedAndFileContentAlreadyExists_returnsStoredFileAndUpdatesLastAccessed() {
            final byte[] expected = "already rendered".getBytes(StandardCharsets.UTF_8);
            final ReportInstanceFileEntity existingFile = ReportInstanceFileEntity.builder()
                .reportInstanceId(1L)
                .fileType(SupportedFileType.CSV)
                .locationUuid(EXISTING_FILE_LOCATION)
                .createdTimestamp(LocalDateTime.parse("2026-10-02T10:15:30"))
                .lastAccessedTimestamp(LocalDateTime.parse("2026-10-02T10:15:30"))
                .build();

            mock_reportInstanceAtLocation(LOCATION);
            mock_reportTemplateLookup();
            mock_storedReportContent(REPORT_JSON);
            mock_hasPermissionForReportAndBusinessUnit();
            when(reportInstanceFileRepository.findByReportInstanceIdAndFileType(1L, SupportedFileType.CSV))
                .thenReturn(Optional.of(existingFile));
            when(reportBlobStore.getReport(EXISTING_FILE_LOCATION)).thenReturn(expected);

            Object actual = getReportInstanceContentService.getReportInstanceContent(1L, CSV);

            assertAll(
                () -> assertArrayEquals(expected, (byte[]) actual),
                () -> verify(reportBlobStore).getReport(EXISTING_FILE_LOCATION),
                () -> assertEquals(LocalDateTime.now(clock), existingFile.getLastAccessedTimestamp()),
                () -> verify(reportInstanceFileRepository).save(existingFile),
                () -> verify(mapper, never()).readValue(anyString(), eq(StoredReportContent.class)),
                () -> verify(reportInterfaceImplementation, never()).convertReportDataToFileType(any(), any(), any()),
                () -> verify(reportBlobStore, never()).storeReport(any())
            );
        }

        @Test
        void retrieveReportContent_whenNoStoredFileExists_returnsEmpty() {
            when(reportInstanceFileRepository.findByReportInstanceIdAndFileType(1L, SupportedFileType.CSV))
                .thenReturn(Optional.empty());

            Optional<byte[]> actual = getReportInstanceContentService.retrieveReportContent(reportInstance, CSV);

            assertAll(
                () -> assertEquals(Optional.empty(), actual),
                () -> verify(reportBlobStore, never()).getReport(any()),
                () -> verify(reportInstanceFileRepository, never()).save(any())
            );
        }

        @Test
        void retrieveReportContent_whenStoredFileExists_returnsContentAndUpdatesLastAccessed() {
            byte[] expected = "cached content".getBytes(StandardCharsets.UTF_8);
            ReportInstanceFileEntity existingFile = ReportInstanceFileEntity.builder()
                .reportInstanceId(1L)
                .fileType(SupportedFileType.CSV)
                .locationUuid(EXISTING_FILE_LOCATION)
                .createdTimestamp(LocalDateTime.parse("2026-10-02T10:15:30"))
                .lastAccessedTimestamp(LocalDateTime.parse("2026-10-02T10:15:30"))
                .build();
            when(reportInstanceFileRepository.findByReportInstanceIdAndFileType(1L, SupportedFileType.CSV))
                .thenReturn(Optional.of(existingFile));
            when(reportBlobStore.getReport(EXISTING_FILE_LOCATION)).thenReturn(expected);

            Optional<byte[]> actual = getReportInstanceContentService.retrieveReportContent(reportInstance, CSV);

            assertAll(
                () -> assertArrayEquals(expected, actual.orElseThrow()),
                () -> verify(reportBlobStore).getReport(EXISTING_FILE_LOCATION),
                () -> assertEquals(LocalDateTime.now(clock), existingFile.getLastAccessedTimestamp()),
                () -> verify(reportInstanceFileRepository).save(existingFile)
            );
        }

        @Test
        void retrieveOrGenerateReportContent_whenExistingReportContentIsPresent_returnsExistingContent() {
            byte[] expected = "cached content".getBytes(StandardCharsets.UTF_8);
            ReportInstanceFileEntity existingFile = ReportInstanceFileEntity.builder()
                .reportInstanceId(1L)
                .fileType(SupportedFileType.CSV)
                .locationUuid(EXISTING_FILE_LOCATION)
                .createdTimestamp(LocalDateTime.parse("2026-10-02T10:15:30"))
                .lastAccessedTimestamp(LocalDateTime.parse("2026-10-02T10:15:30"))
                .build();
            when(reportInstanceFileRepository.findByReportInstanceIdAndFileType(1L, SupportedFileType.CSV))
                .thenReturn(Optional.of(existingFile));
            when(reportBlobStore.getReport(EXISTING_FILE_LOCATION)).thenReturn(expected);

            byte[] actual = getReportInstanceContentService.retrieveOrGenerateReportContent(
                1L,
                reportInstance,
                REPORT_JSON,
                reportInterfaceImplementation,
                CSV
            );

            assertAll(
                () -> assertArrayEquals(expected, actual),
                () -> assertEquals(LocalDateTime.now(clock), existingFile.getLastAccessedTimestamp()),
                () -> verify(reportInstanceFileRepository).save(existingFile),
                () -> verify(mapper, never()).readValue(anyString(), eq(StoredReportContent.class)),
                () -> verify(reportInterfaceImplementation, never()).convertReportDataToFileType(any(), any(), any()),
                () -> verify(reportBlobStore, never()).storeReport(any())
            );
        }

        @Test
        void retrieveOrGenerateReportContent_whenExistingReportContentIsNotPresent_generatesAndStoresContent()
            throws Exception {
            byte[] expected = "generated content".getBytes(StandardCharsets.UTF_8);
            when(reportInstanceFileRepository.findByReportInstanceIdAndFileType(1L, SupportedFileType.CSV))
                .thenReturn(Optional.empty());
            when(mapper.readValue(REPORT_JSON, StoredReportContent.class)).thenReturn(storedReportContent);
            doReturn(GetReportInstanceContentTestData.TestReportData.class)
                .when(reportInterfaceImplementation).getStoredReportDataClass(reportInstance);
            when(mapper.convertValue(Map.of("rows", 2), GetReportInstanceContentTestData.TestReportData.class))
                .thenReturn(reportData);
            when(reportInterfaceImplementation.convertReportDataToFileType(reportInstance, reportData, CSV))
                .thenReturn(expected);
            when(reportBlobStore.storeReport(any())).thenReturn(GENERATED_FILE_LOCATION);

            byte[] actual = getReportInstanceContentService.retrieveOrGenerateReportContent(
                1L,
                reportInstance,
                REPORT_JSON,
                reportInterfaceImplementation,
                CSV
            );

            ArgumentCaptor<InputStream> storedFile = ArgumentCaptor.forClass(InputStream.class);
            ArgumentCaptor<ReportInstanceFileEntity> fileEntity =
                ArgumentCaptor.forClass(ReportInstanceFileEntity.class);

            assertAll(
                () -> assertArrayEquals(expected, actual),
                () -> verify(reportInterfaceImplementation).convertReportDataToFileType(reportInstance, reportData,
                    CSV),
                () -> verify(reportBlobStore).storeReport(storedFile.capture()),
                () -> assertArrayEquals(expected, storedFile.getValue().readAllBytes()),
                () -> verify(reportInstanceFileRepository).save(fileEntity.capture()),
                () -> assertEquals(1L, fileEntity.getValue().getReportInstanceId()),
                () -> assertEquals(SupportedFileType.CSV, fileEntity.getValue().getFileType()),
                () -> assertEquals(GENERATED_FILE_LOCATION, fileEntity.getValue().getLocationUuid()),
                () -> assertEquals(LocalDateTime.now(clock), fileEntity.getValue().getCreatedTimestamp()),
                () -> assertEquals(LocalDateTime.now(clock), fileEntity.getValue().getLastAccessedTimestamp())
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
            when(reportInstanceFileRepository.findByReportInstanceIdAndFileType(1L, SupportedFileType.CSV))
                .thenReturn(Optional.empty());
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

    private void mock_reportInstanceAtLocation(UUID location) {
        reportInstance.setLocation(location);
        when(reportInstanceRepository.findById(1L)).thenReturn(Optional.of(reportInstance));
    }

    private void mock_authenticationContext() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mock_storedReportContent(String reportContent) {
        when(reportBlobStore.getReport(LOCATION)).thenReturn(reportContent.getBytes(StandardCharsets.UTF_8));
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
