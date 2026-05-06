package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportDto;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createDefaultReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createFullReportEntity;
import static uk.gov.hmcts.opal.testdata.ReportTestData.createReportEntityWithNullRetentionPeriod;

import jakarta.persistence.EntityNotFoundException;
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
import uk.gov.hmcts.opal.generated.model.ReportReports;
import uk.gov.hmcts.opal.mapper.ReportEntityMapper;
import uk.gov.hmcts.opal.repository.ReportRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportEntityMapper reportMapper;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private ReportService reportService;

    private UserState stubUserAndRepo(String reportId, Optional<ReportEntity> repoResult) {
        UserState userState = mock(UserState.class);
        when(userStateService.checkForAuthorisedUser()).thenReturn(userState);
        when(reportRepository.findById(reportId)).thenReturn(repoResult);
        return userState;
    }

    @Nested
    class GetReportSuccessCases {

        static Stream<Arguments> successCases() {
            return Stream.of(
                Arguments.of(createDefaultReportEntity(), false),
                Arguments.of(createReportEntityWithNullRetentionPeriod(), false),
                Arguments.of(createFullReportEntity(), true)
            );
        }

        @ParameterizedTest
        @MethodSource("successCases")
        void getReport_success(ReportEntity entity, boolean stubPermission) {
            ReportReports reportDto = createDefaultReportDto();
            UserState userState = stubUserAndRepo(entity.getReportId(), Optional.of(entity));
            when(reportMapper.toDto(entity)).thenReturn(reportDto);
            if (stubPermission) {
                when(userState.anyBusinessUnitUserHasPermission(FinesPermission.REPORTS_VIEW)).thenReturn(true);
            }

            ReportReports result = reportService.getReport(entity.getReportId());

            assertEquals(reportDto, result);
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
                    Optional.empty(),
                    EntityNotFoundException.class,
                    "Report not found with id: non_existent_report"
                ),
                Arguments.of(
                    createFullReportEntity().getReportId(),
                    Optional.of(createFullReportEntity()),
                    PermissionNotAllowedException.class,
                    null
                )
            );
        }

        @ParameterizedTest
        @MethodSource("errorCases")
        void getReport_throwsExpectedException(
            String reportId,
            Optional<ReportEntity> repoResult,
            Class<? extends Exception> expectedException,
            String expectedMessage
        ) {
            stubUserAndRepo(reportId, repoResult);

            Exception exception = assertThrows(expectedException, () -> reportService.getReport(reportId));

            if (expectedMessage != null) {
                assertEquals(expectedMessage, exception.getMessage());
            }
        }
    }
}
