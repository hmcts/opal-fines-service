package uk.gov.hmcts.opal.service.report;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.ReportEntity;

@ExtendWith(MockitoExtension.class)
public class ReportParameterServiceTest {

    @InjectMocks
    ReportParameterService reportParameterService;


    @Mock
    ReportEntity reportEntity;
}
