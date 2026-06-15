package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.ERROR;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;

@ExtendWith(MockitoExtension.class)
class GenericReportServiceTest {

    public static final String LOCATION = "location";

    @InjectMocks
    private GenericReportService genericReportService;

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
    @Mock
    Clock clock;
    ReportInstanceEntity reportInstance;
    @Mock
    ReportEntity reportEntity;
    @Mock
    TestData reportData;
    @Mock
    ObjectMapper mapper;
    @Mock
    ReportInstanceMapper reportInstanceMapper;
    String reportId;
    Instant now;
    @InjectMocks
    private GenericReportService genericReportService;

    @BeforeEach
    void setUp() {
        reportInstance = new ReportInstanceEntity();
        reportId = String.valueOf(UUID.randomUUID());
        reportInstance.setReportId(reportId);
    }

    void mockClock() {
        now = Instant.parse("2026-01-01T10:00:00Z");
        when(clock.instant()).thenReturn(now);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void generateReportInstanceContent_happyPath() throws JsonProcessingException {
        mockClock();

        reportInstance.setReportId(reportId);
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
        assertThat(lastEntity.getReportId()).isEqualTo(reportId);
        assertThat(lastEntity.getGenerationStatus()).isEqualTo(READY);
        assertThat(lastEntity.getLocation()).isEqualTo(LOCATION);
        assertThat(lastEntity.getErrors()).isNull();
        assertThat(lastEntity.getNoOfRecords()).isEqualTo((short) 2);
        assertThat(lastEntity.getCreatedTimestamp()).isEqualTo(LocalDateTime.now(clock));
        assertThat(lastEntity.getScheduledDeletionTimestamp()).isEqualTo(LocalDateTime.now(clock).plusDays(1));
    }

    @Test
    void generateReportInstanceContent_retentionPeriodIsNull_doNotSetDeletionTimestamp()
        throws JsonProcessingException {
        mockClock();

        reportInstance.setReportId(reportId);
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
        mockClock();

        //Arrange
        when(reportInstanceRepository.findById(any())).thenThrow(EntityNotFoundException.class);
        //Act
        assertThrows(EntityNotFoundException.class, () -> genericReportService.generateReportInstanceContent(1L));
        //Assert
        verify(reportInstanceRepository, times(0)).save(reportInstance);
    }

    @Test
    void generateReportInstanceContent_unableToSaveToBlobStore_throwsException() throws JsonProcessingException {
        mockClock();

        //Arrange
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
        mockClock();
        //Arrange
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
        mockClock();
        //Arrange
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInstanceRepository.save(any())).thenThrow(RuntimeException.class);
        when(reportEntity.getReportId()).thenReturn(reportId);
        //Act + Assert
        assertThrows(ReportGenerationException.class, () -> genericReportService.generateReportInstanceContent(1L));
    }

    @Test
    public void addReportInstance_success_singleBU() throws JacksonException {
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

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);
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
    public void addReportInstance_success_multiBU() throws JacksonException {
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

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);
        when(businessUnitUser2.getBusinessUnitId()).thenReturn((short)2);

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

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);
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

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);
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
    public void addReportInstance_genReportAsyncFalse_throwsException() throws JacksonException {
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

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);

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
            matching.setReportId(PERMITTED_REPORT_ID);
            secondMatching.setReportId(PERMITTED_REPORT_ID);
        }

        @Test
        void whenReportIdProvidedButNoPermission_accessDeniedIsThrown_sadPath() {
            mockReportLookup(RESTRICTED_REPORT_ID, restrictedReport);
            mockReportPermission(ACCOUNT_MAINTENANCE, false);

            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> genericReportService.searchReportInstances(FROM_DATE, TO_DATE, null, USER_ID,
                    RESTRICTED_REPORT_ID)
            );

            assertAll(
                () -> Assertions.assertThat(exception.getMessage())
                    .isEqualTo("User does not have permission for reportId: " + RESTRICTED_REPORT_ID),
                () -> verify(reportRepository).findById(RESTRICTED_REPORT_ID),
                () -> verify(reportInstanceRepository, never()).findAll(specificationMatcher())
            );
        }

        @Test
        void whenBusinessUnitsProvidedAndAnyAreNotPermitted_accessDeniedIsThrown_sadPath() {
            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> genericReportService.searchReportInstances(FROM_DATE, TO_DATE, List.of(10, 20), USER_ID, null)
            );

            assertAll(
                () -> Assertions.assertThat(exception.getMessage())
                    .isEqualTo("User does not have permission for one or more specified business units"),
                () -> verify(reportRepository, never()).findAll(),
                () -> verify(reportInstanceRepository, never()).findAll(specificationMatcher())
            );
        }

        @Test
        void whenReportIdAndBusinessUnitsProvidedAndAllPermitted_returnsData_happyPath() {
            mockReportLookup(PERMITTED_REPORT_ID, report);
            mockReportPermission(SEARCH_AND_VIEW_ACCOUNTS, true);
            mockUserStateWithSomeBusinessUnitsPermitted();
            mockBusinessUnitUsersForIds(
                List.of(10L),
                List.of(businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS))
            );
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
                () -> verify(reportRepository).findById(PERMITTED_REPORT_ID),
                () -> verify(userStateService).getBusinessUnitUsersForBusinessUnitIds(List.of(10L)),
                () -> verify(reportInstanceRepository).findAll(specificationMatcher()),
                () -> verify(reportInstanceMapper).toDto(matching, report)
            );
        }

        @Test
        void whenReportIdNotProvided_filtersOnlyPermittedReportIds_happyPath() {
            when(reportRepository.findAll()).thenReturn(List.of(report, restrictedReport));
            mockCurrentUserBusinessUnits(List.of(businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS)));
            mockBusinessUnitUsersForIds(
                List.of(10L),
                List.of(businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS))
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
                () -> verify(reportRepository).findAll(),
                () -> verify(reportInstanceRepository).findAll(specificationMatcher()),
                () -> verify(reportInstanceMapper).toDto(matching, report),
                () -> verify(reportRepository, never()).findById(any())
            );
        }

        @Test
        void whenBusinessUnitsNotProvided_filtersOnlyAccessibleBusinessUnits_happyPath() {
            mockReportLookup(PERMITTED_REPORT_ID, report);
            mockReportPermission(SEARCH_AND_VIEW_ACCOUNTS, true);
            List<BusinessUnitUser> businessUnitUsers = List.of(
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("20", SEARCH_AND_VIEW_ACCOUNTS)
            );
            mockCurrentUserBusinessUnits(businessUnitUsers);
            mockBusinessUnitUsersForIds(List.of(10L, 20L), businessUnitUsers);
            mockReportInstancesFound(List.of(matching, secondMatching));
            mockDtoMapped();
            when(reportInstanceMapper.toDto(secondMatching, report)).thenReturn(secondDto);

            List<ReportInstanceListReportsInner> result =
                genericReportService.searchReportInstances(FROM_DATE, TO_DATE, null, USER_ID, PERMITTED_REPORT_ID);

            assertAll(
                () -> Assertions.assertThat(result).containsExactly(dto, secondDto),
                () -> verify(userStateService).getAllBusinessUnitUsersForCurrentUser(),
                () -> verify(userStateService).getBusinessUnitUsersForBusinessUnitIds(List.of(10L, 20L)),
                () -> verify(reportInstanceRepository).findAll(specificationMatcher())
            );
        }

        @Test
        void whenNoPermittedReportBusinessUnitMappingExists_emptyListIsReturned_happyPath() {
            when(reportRepository.findAll()).thenReturn(List.of(restrictedReport));
            List<BusinessUnitUser> businessUnitUsers =
                List.of(businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS));
            mockCurrentUserBusinessUnits(businessUnitUsers);
            mockBusinessUnitUsersForIds(List.of(10L), businessUnitUsers);

            List<ReportInstanceListReportsInner> result =
                genericReportService.searchReportInstances(FROM_DATE, TO_DATE, null, USER_ID, null);

            assertAll(
                () -> Assertions.assertThat(result).isEmpty(),
                () -> verify(reportInstanceRepository, never()).findAll(specificationMatcher()),
                () -> verify(reportInstanceMapper, never()).toDto(any(), any())
            );
        }

        private void mockReportInstancesFound(List<ReportInstanceEntity> reportInstances) {
            when(reportInstanceRepository.findAll(specificationMatcher())).thenReturn(reportInstances);
        }

        private void mockReportLookup(String reportId, ReportEntity reportEntity) {
            when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        }

        private void mockCurrentUserBusinessUnits(List<BusinessUnitUser> businessUnitUsers) {
            when(userStateService.getAllBusinessUnitUsersForCurrentUser()).thenReturn(businessUnitUsers);
        }

        private void mockBusinessUnitUsersForIds(List<Long> businessUnitIds, List<BusinessUnitUser> businessUnitUsers) {
            when(userStateService.getBusinessUnitUsersForBusinessUnitIds(businessUnitIds)).thenReturn(
                businessUnitUsers);
        }

        private void mockUserStateWithSomeBusinessUnitsPermitted() {
            when(userStateService.getAllBusinessUnitUsersForCurrentUser()).thenReturn(
                List.of(businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS),
                    businessUnitUserWithPermission("20", SEARCH_AND_VIEW_ACCOUNTS)));
        }

        private void mockBusinessUnitPermissions(List<Integer> businessUnitIds, boolean permitted) {
            businessUnitIds.forEach(
                businessUnitId -> when(
                    userStateService.isBusinessUnitPermittedForCurrentUser(businessUnitId.shortValue()))
                    .thenReturn(permitted)
            );
        }

        private void mockDtoMapped() {
            when(reportInstanceMapper.toDto(matching, report)).thenReturn(dto);
        }

        private void mockReportPermission(FinesPermission permission, boolean permitted) {
            when(userStateService.checkAnyBusinessUnitUserHasPermission(permission)).thenReturn(permitted);
        }

        private Specification<ReportInstanceEntity> specificationMatcher() {
            return org.mockito.ArgumentMatchers.any();
        }
    }

}
