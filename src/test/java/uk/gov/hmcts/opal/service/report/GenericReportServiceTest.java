package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.ACCOUNT_MAINTENANCE;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.ERROR;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.DEFAULT_REPORT_ID;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.FROM_DATE;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.TO_DATE;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.USER_ID;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.createDefaultReportInstanceEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportEntity;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.exc.StreamConstraintsException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.generated.model.ReportInstanceReports;
import uk.gov.hmcts.opal.mapper.ReportInstanceMapper;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.gov.hmcts.opal.service.messaging.ReportQueuePublisher;

@ExtendWith(MockitoExtension.class)
class GenericReportServiceTest {
    public static final String LOCATION = "location";
    private final Map<String, Object> reportParameters = Map.of("foo", "bar");
    private final CreateReportInstanceResponseReports reportInstanceResponse =
        CreateReportInstanceResponseReports.builder().reportInstanceId(123L).build();
    String reportId;
    Instant now;

    @Mock
    ReportInstanceRepository reportInstanceRepository;
    @Mock
    ReportRepository reportRepository;
    @Mock
    ReportBlobStore reportBlobStore;
    @Mock
    ReportRegistry reportRegistry;
    @SuppressWarnings("rawtypes")
    @Mock
    ReportInterface reportInterfaceImplementation;
    Clock clock = Clock.fixed(Instant.parse("2026-04-22T00:00:00Z"), ZoneOffset.UTC);
    ReportInstanceEntity reportInstance;
    @Mock
    ReportEntity reportEntity;
    @Mock
    TestData reportData;
    @Mock
    ObjectMapper mapper;
    @Mock
    ReportInstanceMapper reportInstanceMapper;
    @Mock
    ReportInstanceSearchService reportInstanceSearchService;
    @Mock
    private UserStateService userStateService;
    @Mock
    private UserState userState;
    @Mock
    private ReportParameterValidator reportParameterValidator;
    @Mock
    private ReportQueuePublisher reportQueuePublisher;
    @Mock
    private BusinessUnitUser businessUnitUser1;
    @Mock
    private BusinessUnitUser businessUnitUser2;

    @Mock
    BusinessUnitRepository businessUnitRepository;

    @Mock
    BusinessUnitEntity businessUnitEntity1;

    @Mock
    BusinessUnitEntity businessUnitEntity2;

    @Mock
    ReportInstanceReports reportInstanceReports;

    private GenericReportService genericReportService;

    @BeforeEach
    void setUp() {
        reportInstance = new ReportInstanceEntity();
        reportId = String.valueOf(UUID.randomUUID());
        reportInstance.setReport(reportEntity);
        now = clock.instant();
        genericReportService = new GenericReportService(
            reportParameterValidator,
            reportQueuePublisher,
            userStateService,
            reportInstanceRepository,
            reportRepository,
            reportInstanceMapper,
            reportRegistry,
            reportBlobStore,
            clock,
            mapper,
            businessUnitRepository,
            reportInstanceSearchService
        );
    }

    @Test
    void generateReportInstanceContent_happyPath() {
        when(reportEntity.getReportId()).thenReturn(reportId);
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInterfaceImplementation.generateReportData(reportInstance)).thenReturn(reportData);
        when(reportData.getNumberOfRecords()).thenReturn(2L);
        when(reportBlobStore.storeReport(any())).thenReturn(LOCATION);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getRetentionPeriod()).thenReturn(Duration.ofDays(1));
        //Act
        genericReportService.generateReportInstanceContent(1L);
        //Assert
        ArgumentCaptor<String> toSaveInBlobStore = ArgumentCaptor.forClass(String.class);
        verify(reportBlobStore).storeReport(toSaveInBlobStore.capture());
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(2)).save(entities.capture());

        ReportInstanceEntity lastEntity = entities.getAllValues().getLast();
        assertThat(lastEntity.getReport().getReportId()).isEqualTo(reportId);
        assertThat(lastEntity.getGenerationStatus()).isEqualTo(READY);
        assertThat(lastEntity.getLocation()).isEqualTo(LOCATION);
        assertThat(lastEntity.getErrors()).isNull();
        assertThat(lastEntity.getNoOfRecords()).isEqualTo(2L);
        assertThat(lastEntity.getCreatedTimestamp()).isEqualTo(LocalDateTime.now(clock));
        assertThat(lastEntity.getScheduledDeletionTimestamp()).isEqualTo(LocalDateTime.now(clock).plusDays(1));
    }

    @Test
    void generateReportInstanceContent_retentionPeriodIsNull_doNotSetDeletionTimestamp() {
        ReportEntity instanceReport = new ReportEntity();
        instanceReport.setReportId(reportId);
        reportInstance.setReport(instanceReport);
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInterfaceImplementation.generateReportData(reportInstance)).thenReturn(reportData);
        when(reportData.getNumberOfRecords()).thenReturn(2L);
        when(reportBlobStore.storeReport(any())).thenReturn(LOCATION);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getRetentionPeriod()).thenReturn(null);
        //Act
        genericReportService.generateReportInstanceContent(1L);
        //Assert
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(2)).save(entities.capture());
        ReportInstanceEntity lastEntity = entities.getAllValues().getLast();
        assertNull(lastEntity.getScheduledDeletionTimestamp());
    }

    @Test
    void generateReportInstanceContent_reportIdNotFound_throwsExceptionAndDoesNotSaveAnEmptyEntity() {
        //Arrange
        when(reportInstanceRepository.findById(any())).thenThrow(EntityNotFoundException.class);
        //Act
        assertThrows(EntityNotFoundException.class, () -> genericReportService.generateReportInstanceContent(1L));
        //Assert
        verify(reportInstanceRepository, times(0)).save(reportInstance);
    }

    @Test
    void generateReportInstanceContent_unableToSaveToBlobStore_throwsException() {
        //Arrange
        when(reportEntity.getReportId()).thenReturn(reportId);
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInterfaceImplementation.generateReportData(reportInstance)).thenReturn(reportData);
        when(reportBlobStore.storeReport(any())).thenThrow(RuntimeException.class);
        //Act
        assertThrows(ReportGenerationException.class, () -> genericReportService.generateReportInstanceContent(1L));
        //Assert
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(2)).save(entities.capture());
        ReportInstanceEntity savedEntity = entities.getAllValues().getLast();
        assertThat(savedEntity.getGenerationStatus()).isEqualTo(ERROR);
        assertThat(savedEntity.getErrors()).isInstanceOf(ReportError.class);
    }

    @Test
    void generateReportInstanceContent_noImplementationForReportIdFound_throwsException() {
        //Arrange
        when(reportEntity.getReportId()).thenReturn(reportId);
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenThrow(ReportNotFoundException.class);
        //Act
        assertThrows(ReportGenerationException.class, () -> genericReportService.generateReportInstanceContent(1L));
        //Assert
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(1)).save(entities.capture());
        ReportInstanceEntity savedEntity = entities.getAllValues().getFirst();
        assertThat(savedEntity.getGenerationStatus()).isEqualTo(ERROR);
        assertThat(savedEntity.getErrors()).isInstanceOf(ReportError.class);
    }

    @Test
    void generateReportInstanceContent_unableToSaveToDb_throwsException() {
        //Arrange
        when(reportEntity.getReportId()).thenReturn(reportId);
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInstanceRepository.save(any())).thenThrow(RuntimeException.class);
        //Act + Assert
        assertThrows(ReportGenerationException.class, () -> genericReportService.generateReportInstanceContent(1L));
    }

    @Test
    void getReportInstance_reportInstanceNotFound_throwsException() {
        when(reportInstanceRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> genericReportService.getReportInstance(1L));

        assertEquals("Report instance not found with id: 1", exception.getMessage());
    }

    @Test
    void getReportInstance_withMatchingBusinessUnits_returnsReportInstance() {
        when(reportInstanceRepository.findById(1L)).thenReturn(Optional.of(reportInstance));
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1, businessUnitUser2));
        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short) 1);
        when(businessUnitUser2.getBusinessUnitId()).thenReturn((short) 2);
        reportInstance.setBusinessUnit(List.of(1, 2));
        when(businessUnitRepository.findAllById(List.of((short) 1, (short) 2))).thenReturn(
            List.of(businessUnitEntity1, businessUnitEntity2));
        when(reportInstanceMapper.toReportInstanceReportsDto(reportInstance, List.of(businessUnitEntity1,
            businessUnitEntity2))).thenReturn(reportInstanceReports);

        ReportInstanceReports result = genericReportService.getReportInstance(1L);

        assertThat(result).isEqualTo(reportInstanceReports);
        verify(businessUnitRepository).findAllById(List.of((short) 1, (short) 2));
        verify(reportInstanceMapper).toReportInstanceReportsDto(reportInstance, List.of(businessUnitEntity1,
            businessUnitEntity2));
    }

    @Test
    void getReportInstance_withForeignBusinessUnits_throwsAccessDenied() {
        when(reportInstanceRepository.findById(1L)).thenReturn(Optional.of(reportInstance));
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short) 1);
        reportInstance.setBusinessUnit(List.of(1, 2));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> genericReportService.getReportInstance(1L));

        assertEquals("You cannot request report instances associated with other business units",
            exception.getMessage());
    }

    @Test
    public void addReportInstance_success_singleBU() {
        //setup
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.isSupportsMultiBu()).thenReturn(false);
        when(reportEntity.isCanManuallyCreate()).thenReturn(true);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(reportInstanceRepository.save(any())).thenReturn(reportInstance);
        when(reportInstanceMapper.toResponseDto(reportInstance)).thenReturn(reportInstanceResponse);
        when(reportParameterValidator.validateReportInstanceParameterValues(reportParameters, reportEntity))
            .thenReturn(true);

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short) 1);
        reportInstance.setReportInstanceId(123L);

        //test
        assertThat(genericReportService.addReportInstance(
            CreateReportInstanceRequestReports.builder()
                .reportId(reportId)
                .reportName(null)
                .businessUnitIds(List.of(1))
                .reportParameters(reportParameters)
                .build(), true)).isEqualTo(reportInstanceResponse);

        verify(reportQueuePublisher).publish(123L);
    }

    @Test
    public void addReportInstance_success_multiBU() {
        //setup
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1, businessUnitUser2));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.isSupportsMultiBu()).thenReturn(true);
        when(reportEntity.isCanManuallyCreate()).thenReturn(true);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(reportInstanceRepository.save(any())).thenReturn(reportInstance);
        when(reportInstanceMapper.toResponseDto(reportInstance)).thenReturn(reportInstanceResponse);
        when(reportParameterValidator.validateReportInstanceParameterValues(reportParameters, reportEntity))
            .thenReturn(true);

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short) 1);
        when(businessUnitUser2.getBusinessUnitId()).thenReturn((short) 2);

        reportInstance.setReportInstanceId(123L);

        //test
        assertThat(genericReportService.addReportInstance(
            CreateReportInstanceRequestReports.builder()
                .reportId(reportId)
                .reportName(null)
                .businessUnitIds(List.of(1, 2))
                .reportParameters(reportParameters)
                .build(), true)).isEqualTo(reportInstanceResponse);

        verify(reportQueuePublisher).publish(123L);
    }

    @Test
    public void addReportInstance_multiBU_notAllowed_throwsException() {
        //setup
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.isSupportsMultiBu()).thenReturn(false);

        //test
        UnprocessableException exception = assertThrows(UnprocessableException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1, 2))
                    .reportParameters(reportParameters)
                    .build(), true));
        assertEquals("Too many business units supplied, this report only allows 1", exception.getDetailedReason());
    }

    @Test
    public void addReportInstance_noManualCreation_throwsException() {
        //setup
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.isSupportsMultiBu()).thenReturn(false);
        when(reportEntity.isCanManuallyCreate()).thenReturn(false);

        //test
        UnprocessableException exception = assertThrows(UnprocessableException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1))
                    .reportParameters(reportParameters)
                    .build(), true));
        assertEquals("This report cannot be manually created", exception.getDetailedReason());
    }

    @Test
    public void addReportInstance_userNotAuthorizedWithBU_throwsException() {
        //setup
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1, businessUnitUser2));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.isSupportsMultiBu()).thenReturn(true);
        when(reportEntity.isCanManuallyCreate()).thenReturn(true);
        //when(mapper.writeValueAsString(any())).thenReturn("{}");

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short) 1);
        //test
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1, 2))
                    .reportParameters(reportParameters)
                    .build(), true));
        assertEquals("You cannot generate reports for other business units", exception.getMessage());
    }

    @Test
    public void addReportInstance_failsValidation_throwsException() {
        //setup
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1, businessUnitUser2));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.isSupportsMultiBu()).thenReturn(true);
        when(reportEntity.isCanManuallyCreate()).thenReturn(true);
        //when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(reportParameterValidator.validateReportInstanceParameterValues(reportParameters, reportEntity))
            .thenReturn(false);

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short) 1);
        //test
        UnprocessableException exception = assertThrows(UnprocessableException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1))
                    .reportParameters(reportParameters)
                    .build(), true));
        assertEquals("Validation failed for report instance parameters", exception.getDetailedReason());
    }

    @Test
    public void addReportInstance_genReportAsyncFalse_throwsException() {
        //setup
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.isSupportsMultiBu()).thenReturn(false);
        when(reportEntity.isCanManuallyCreate()).thenReturn(true);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(reportInstanceRepository.save(any())).thenReturn(reportInstance);
        when(reportParameterValidator.validateReportInstanceParameterValues(reportParameters, reportEntity))
            .thenReturn(true);

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short) 1);

        //test
        UnprocessableException exception = assertThrows(UnprocessableException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1))
                    .reportParameters(reportParameters)
                    .build(), false));
        assertEquals("generateReportContentAsync cannot be false", exception.getDetailedReason());
    }

    @Test
    public void addReportInstance_invalidJson_throwsException() {
        //setup
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.isSupportsMultiBu()).thenReturn(false);
        when(reportEntity.isCanManuallyCreate()).thenReturn(true);
        when(mapper.writeValueAsString(any())).thenThrow(new StreamConstraintsException("unit test"));
        when(reportParameterValidator.validateReportInstanceParameterValues(reportParameters, reportEntity))
            .thenReturn(true);

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short) 1);
        reportInstance.setReportInstanceId(123L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1))
                    .reportParameters(reportParameters)
                    .build(), false));
        assertEquals("Report parameters badly formatted", exception.getMessage());
    }

    static class TestData implements ReportDataInterface {

        @Override
        public long getNumberOfRecords() {
            return 0;
        }

        @Override
        public ReportMetaData getReportMetaData() {
            return null;
        }
    }

    @Nested
    class SearchReportInstances {

        private static final String RESTRICTED_REPORT_ID = "restricted_report";
        private static final String PERMITTED_REPORT_ID = DEFAULT_REPORT_ID;

        private final ReportInstanceEntity matching = createDefaultReportInstanceEntity();
        private final ReportInstanceEntity secondMatching = createDefaultReportInstanceEntity();
        private final ReportEntity report = createDefaultReportEntity();
        private final ReportEntity restrictedReport = createDefaultReportEntity();
        private final ReportInstanceListReportsInner dto = new ReportInstanceListReportsInner();
        private final ReportInstanceListReportsInner secondDto = new ReportInstanceListReportsInner();

        @BeforeEach
        void setUp() {
            report.setReportId(PERMITTED_REPORT_ID);
            report.setPermission(SEARCH_AND_VIEW_ACCOUNTS);
            restrictedReport.setReportId(RESTRICTED_REPORT_ID);
            restrictedReport.setPermission(ACCOUNT_MAINTENANCE);
            matching.setReport(report);
            secondMatching.setReport(report);
        }

        @Test
        void whenReportIdProvidedButNoPermission_accessDeniedIsThrown_sadPath() {
            when(reportInstanceSearchService.findRequestedReportElseThrowError(RESTRICTED_REPORT_ID))
                .thenThrow(new AccessDeniedException("User does not have permission for reportId: "
                    + RESTRICTED_REPORT_ID));

            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> genericReportService.searchReportInstances(FROM_DATE, TO_DATE, null, USER_ID,
                    RESTRICTED_REPORT_ID)
            );

            assertAll(
                () -> Assertions.assertThat(exception.getMessage())
                    .isEqualTo("User does not have permission for reportId: " + RESTRICTED_REPORT_ID),
                () -> verify(reportInstanceSearchService).findRequestedReportElseThrowError(RESTRICTED_REPORT_ID),
                () -> verify(reportInstanceRepository, never()).findAll(specificationMatcher())
            );
        }

        @Test
        void whenBusinessUnitsProvidedAndAnyAreNotPermitted_accessDeniedIsThrown_sadPath() {
            when(reportInstanceSearchService.findPermittedReports()).thenReturn(List.of(report));
            when(reportInstanceSearchService.validateBusinessUnitIds(List.of(10, 20)))
                .thenThrow(new AccessDeniedException("User does not have permission for business unit: 20"));

            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> genericReportService.searchReportInstances(FROM_DATE, TO_DATE, List.of(10, 20), USER_ID, null)
            );

            assertAll(
                () -> Assertions.assertThat(exception.getMessage())
                    .isEqualTo("User does not have permission for business unit: 20"),
                () -> verify(reportInstanceSearchService).validateBusinessUnitIds(List.of(10, 20)),
                () -> verify(reportInstanceRepository, never()).findAll(specificationMatcher())
            );
        }

        @Test
        void whenReportIdAndBusinessUnitsProvidedAndAllPermitted_returnsData_happyPath() {
            when(reportInstanceSearchService.findRequestedReportElseThrowError(PERMITTED_REPORT_ID))
                .thenReturn(report);
            when(reportInstanceSearchService.validateBusinessUnitIds(List.of(10))).thenReturn(List.of(10L));
            mock_permittedReportForBusinessUnits(Map.of(PERMITTED_REPORT_ID, List.of(10L)));
            mockReportInstancesFound(List.of(matching));
            mockDtoMapped();

            List<ReportInstanceListReportsInner> result = genericReportService.searchReportInstances(
                FROM_DATE,
                TO_DATE,
                List.of(10),
                USER_ID,
                PERMITTED_REPORT_ID
            );

            assertAll(
                () -> Assertions.assertThat(result).hasSize(1),
                () -> Assertions.assertThat(result).containsExactly(dto),
                () -> verify(reportInstanceSearchService).findRequestedReportElseThrowError(PERMITTED_REPORT_ID),
                () -> verify(reportInstanceSearchService).findPermittedReportForBusinessUnits(
                    List.of(report),
                    List.of(10L)
                ),
                () -> verify(reportInstanceRepository).findAll(specificationMatcher()),
                () -> verify(reportInstanceMapper).toReportInstanceListReportsInnerDto(matching)
            );
        }

        @Test
        void whenReportIdNotProvided_filtersOnlyPermittedReportIds_happyPath() {
            when(reportInstanceSearchService.findPermittedReports()).thenReturn(List.of(report));
            when(reportInstanceSearchService.validateBusinessUnitIds(null)).thenReturn(List.of(10L));
            mock_permittedReportForBusinessUnits(
                Map.of(PERMITTED_REPORT_ID, List.of(10L))
            );
            mockReportInstancesFound(List.of(matching));
            mockDtoMapped();

            List<ReportInstanceListReportsInner> result = genericReportService.searchReportInstances(
                FROM_DATE,
                TO_DATE,
                null,
                USER_ID,
                null
            );

            assertAll(
                () -> Assertions.assertThat(result).hasSize(1),
                () -> Assertions.assertThat(result).containsExactly(dto),
                () -> verify(reportInstanceSearchService).findPermittedReports(),
                () -> verify(reportInstanceRepository).findAll(specificationMatcher()),
                () -> verify(reportInstanceMapper).toReportInstanceListReportsInnerDto(matching)
            );
        }

        @Test
        void whenBusinessUnitsNotProvided_filtersOnlyAccessibleBusinessUnits_happyPath() {
            when(reportInstanceSearchService.findRequestedReportElseThrowError(PERMITTED_REPORT_ID))
                .thenReturn(report);
            when(reportInstanceSearchService.validateBusinessUnitIds(null)).thenReturn(List.of(10L, 20L));
            mock_permittedReportForBusinessUnits(Map.of(PERMITTED_REPORT_ID, List.of(10L, 20L)));
            mockReportInstancesFound(List.of(matching, secondMatching));
            mockDtoMapped();
            when(reportInstanceMapper.toReportInstanceListReportsInnerDto(secondMatching)).thenReturn(secondDto);

            List<ReportInstanceListReportsInner> result =
                genericReportService.searchReportInstances(FROM_DATE, TO_DATE, null, USER_ID, PERMITTED_REPORT_ID);

            assertAll(
                () -> Assertions.assertThat(result).containsExactly(dto, secondDto),
                () -> verify(reportInstanceSearchService).findRequestedReportElseThrowError(PERMITTED_REPORT_ID),
                () -> verify(reportInstanceSearchService).validateBusinessUnitIds(null),
                () -> verify(reportInstanceSearchService).findPermittedReportForBusinessUnits(
                    List.of(report),
                    List.of(10L, 20L)
                ),
                () -> verify(reportInstanceRepository).findAll(specificationMatcher())
            );
        }

        @Test
        void whenNoPermittedReportBusinessUnitMappingExists_emptyListIsReturned_happyPath() {
            when(reportInstanceSearchService.findPermittedReports()).thenReturn(List.of(restrictedReport));
            when(reportInstanceSearchService.validateBusinessUnitIds(null)).thenReturn(List.of(10L));
            mock_permittedReportForBusinessUnits(Map.of());

            List<ReportInstanceListReportsInner> result =
                genericReportService.searchReportInstances(FROM_DATE, TO_DATE, null, USER_ID, null);

            assertAll(
                () -> Assertions.assertThat(result).isEmpty(),
                () -> verify(reportInstanceRepository, never()).findAll(specificationMatcher()),
                () -> verify(reportInstanceMapper, never()).toReportInstanceListReportsInnerDto(any())
            );
        }

        private void mockReportInstancesFound(List<ReportInstanceEntity> reportInstances) {
            when(reportInstanceRepository.findAll(specificationMatcher())).thenReturn(reportInstances);
        }

        private void mockDtoMapped() {
            when(reportInstanceMapper.toReportInstanceListReportsInnerDto(matching)).thenReturn(dto);
        }

        private void mock_permittedReportForBusinessUnits(Map<String, List<Long>> permittedReports) {
            when(reportInstanceSearchService.findPermittedReportForBusinessUnits(any(), any())).thenReturn(
                permittedReports
            );
        }

        private Specification<ReportInstanceEntity> specificationMatcher() {
            return org.mockito.ArgumentMatchers.any();
        }
    }

}
