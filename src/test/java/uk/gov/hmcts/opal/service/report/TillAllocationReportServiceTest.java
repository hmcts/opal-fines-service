package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.InterfaceFileSourceEnum;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.repository.TillRepository;

@ExtendWith(MockitoExtension.class)
class TillAllocationReportServiceTest {

    private static final Long TILL_ID = 123L;
    private static final Short BUSINESS_UNIT_ID = 78;
    private static final Long REQUESTER_USER_ID = 42L;
    private static final String REQUESTER_NAME = "Opal User";

    @Mock
    private TillRepository tillRepository;
    @Mock
    private GenericReportService reportService;
    @InjectMocks
    private TillAllocationReportService service;

    private TillEntity till;

    @BeforeEach
    void setUp() {
        till = TillEntity.builder().tillId(TILL_ID).tillNumber((short) 1)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID).build()).build();
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(till));
    }

    @ParameterizedTest
    @CsvSource({"true,NATWEST,1 - Auto - NATWEST", "false,NATWEST,1 - Manual - NATWEST",
        "false,,1 - Manual", "true,,1 - Auto"})
    void createsBothReports(boolean autoPayment, InterfaceFileSourceEnum source, String title) {
        till.setAutoPayment(autoPayment);
        till.setSource(source);

        service.createReports(TILL_ID, REQUESTER_USER_ID, REQUESTER_NAME);

        InOrder order = inOrder(reportService);
        order.verify(reportService).addReportInstance(eq(request("cash_till",
            Map.of("till_id", TILL_ID,
                "allocated_report", true),
                "Cash till report - Allocated (1)")),
            eq(REQUESTER_USER_ID), eq(REQUESTER_NAME), eq(false));

        order.verify(reportService).addReportInstance(eq(request("cash_list", Map.of("till_number", 1), title)),
            eq(REQUESTER_USER_ID), eq(REQUESTER_NAME), eq(false));
    }

    @Test
    void reportFailureStopsProcessing() {
        IllegalStateException failure = new IllegalStateException("Report failed");
        when(reportService.addReportInstance(any(), eq(REQUESTER_USER_ID), eq(REQUESTER_NAME), eq(false)))
            .thenThrow(failure);

        assertThatThrownBy(() -> service.createReports(TILL_ID, REQUESTER_USER_ID, REQUESTER_NAME)).isSameAs(failure);

        InOrder order = inOrder(reportService);
        order.verify(reportService).addReportInstance(any(), eq(REQUESTER_USER_ID), eq(REQUESTER_NAME), eq(false));
        order.verifyNoMoreInteractions();
    }

    private CreateReportInstanceRequestReports request(String reportId, Map<String, Object> parameters, String title) {
        return CreateReportInstanceRequestReports.builder().reportId(reportId)
            .businessUnitIds(List.of(BUSINESS_UNIT_ID)).reportParameters(parameters).reportName(title).build();
    }
}
