package uk.gov.hmcts.opal.mapper.common;


import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.Payment;

public class PaymentMapperTest {
    @Test
    public void testPaymentIsNull() {
        PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);
        Payment payment = mapper.toDto(null);

        assertNull(payment);
    }

}
