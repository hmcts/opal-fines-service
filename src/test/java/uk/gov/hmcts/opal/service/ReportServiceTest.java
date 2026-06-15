package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportDto;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createFullReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createReportEntityWithNullRetentionPeriod;
import static uk.gov.hmcts.opal.testdata.ReportTestData.defaultReportEntityBuilder;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;
import uk.gov.hmcts.opal.exception.SchemaConfigurationException;
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.mapper.ReportEntityMapper;
import uk.gov.hmcts.opal.repository.ConfigurationItemRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportEntityMapper reportMapper;

    @Mock
    private ConfigurationItemRepository configurationItemRepository;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private ReportService reportService;

    private UserState stubUserAndRepo(String reportId, ReportEntity repoResult) {
        UserState userState = mock(UserState.class);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(reportRepository.findById(reportId)).thenReturn(Optional.ofNullable(repoResult));
        return userState;
    }

    @Nested
    class GetReportSuccessCases {

        static Stream<Arguments> successCases() {
            return Stream.of(
                Arguments.of(createDefaultReportEntity()),
                Arguments.of(createReportEntityWithNullRetentionPeriod()),
                Arguments.of(createFullReportEntity())
            );
        }

        @ParameterizedTest
        @MethodSource("successCases")
        void getReport_success(ReportEntity entity) {
            ReportReports reportDto = createDefaultReportDto();
            UserState userState = stubUserAndRepo(entity.getReportId(), entity);
            when(reportMapper.toDto(entity)).thenReturn(reportDto);
            when(userState.anyBusinessUnitUserHasPermission(null)).thenReturn(true);
            when(userState.anyBusinessUnitUserHasPermission(any(FinesPermission.class))).thenReturn(true);

            if ("operational_report_enforcement".equals(entity.getReportId())
                || "operational_report_payment".equals(entity.getReportId())) {
                when(configurationItemRepository.findByItemNameAndBusinessUnitIdIsNull(
                    "OPERATIONAL_REPORT_BU_WARNING_THRESHOLD"
                )).thenReturn(Optional.of(
                    ConfigurationItemEntity.builder().itemValue("10").build()
                ));
            }

            ReportReports result = reportService.getReport(entity.getReportId());

            if ("operational_report_enforcement".equals(entity.getReportId())
                || "operational_report_payment".equals(entity.getReportId())) {
                Map<String, Object> expectedParameters = new HashMap<>();
                if (reportDto.getReportParameters() != null) {
                    expectedParameters.putAll(reportDto.getReportParameters());
                }
                expectedParameters.put("business_unit_warning_threshold", 10);
                assertEquals(expectedParameters, result.getReportParameters());
            } else {
                assertEquals(reportDto, result);
            }
            verify(reportRepository).findById(entity.getReportId());
            verify(reportMapper).toDto(entity);
        }
    }

    @Nested
    class GetReportErrorCases {

        static Stream<Arguments> errorCases() {
            return Stream.of(
                Arguments.of(
                    "non_existent_report",
                    null,
                    EntityNotFoundException.class,
                    "Report not found with id: non_existent_report"
                ),
                Arguments.of(
                    createFullReportEntity().getReportId(),
                    createFullReportEntity(),
                    PermissionNotAllowedException.class,
                    null
                ),
                Arguments.of(
                    createFullReportEntity().getReportId(),
                    defaultReportEntityBuilder()
                        .permission(null)
                        .build(),
                    PermissionNotAllowedException.class,
                    null
                ),
                Arguments.of(
                    "operational_report_enforcement",
                    defaultReportEntityBuilder()
                        .reportId("operational_report_enforcement")
                        .permission(null)
                        .build(),
                    SchemaConfigurationException.class,
                    "Missing configuration item: OPERATIONAL_REPORT_BU_WARNING_THRESHOLD"
                ),
                Arguments.of(
                    "operational_report_payment",
                    defaultReportEntityBuilder()
                        .reportId("operational_report_payment")
                        .permission(null)
                        .build(),
                    SchemaConfigurationException.class,
                    "Invalid integer configuration item: OPERATIONAL_REPORT_BU_WARNING_THRESHOLD"
                )
            );
        }

        @ParameterizedTest
        @MethodSource("errorCases")
        void getReport_throwsExpectedException(
            String reportId,
            ReportEntity entity,
            Class<? extends Exception> expectedException,
            String expectedMessage
        ) {
            UserState userState = stubUserAndRepo(reportId, entity);
            if (entity != null) {
                when(reportMapper.toDto(entity)).thenReturn(createDefaultReportDto());
                when(userState.anyBusinessUnitUserHasPermission(null)).thenReturn(true);
                when(userState.anyBusinessUnitUserHasPermission(any(FinesPermission.class))).thenReturn(true);
            }

            if ("operational_report_payment".equals(reportId)) {
                when(configurationItemRepository.findByItemNameAndBusinessUnitIdIsNull(
                    "OPERATIONAL_REPORT_BU_WARNING_THRESHOLD"
                )).thenReturn(Optional.of(
                    ConfigurationItemEntity.builder().itemValue("not-an-integer").build()
                ));
            }

            Exception exception = assertThrows(expectedException, () -> reportService.getReport(reportId));

            if (expectedMessage != null) {
                assertEquals(expectedMessage, exception.getMessage());
            }
        }
    }
}
