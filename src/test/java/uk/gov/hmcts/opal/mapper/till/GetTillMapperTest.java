package uk.gov.hmcts.opal.mapper.till;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.mapper.till.GetTillMapperTestData.CREATED_DATE;
import static uk.gov.hmcts.opal.mapper.till.GetTillMapperTestData.till;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.generated.model.TillsGetResponse;

@SpringJUnitConfig(GetTillMapperTestConfiguration.class)
class GetTillMapperTest {

    @Autowired
    private GetTillMapper mapper;

    @Test
    void whenTillIsProvided_mapsTopLevelResponse_happyPath() {
        TillsGetResponse response = mapper.toResponse(till(), List.of(), Map.of());

        assertAll(
            () -> assertEquals((short) 42, response.getTillNumber()),
            () -> assertEquals((short) 77, response.getBusinessUnitId()),
            () -> assertEquals("Alex Cashier", response.getCreatedBy()),
            () -> assertEquals(CREATED_DATE, response.getCreatedDate()),
            () -> assertEquals(List.of(), response.getPaymentsIn())
        );
    }
}
