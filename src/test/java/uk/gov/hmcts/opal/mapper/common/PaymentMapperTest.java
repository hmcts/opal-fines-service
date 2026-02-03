package uk.gov.hmcts.opal.mapper.common;


import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.dto.Payment;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PaymentMapperTest.MapperTestConfig.class)
public class PaymentMapperTest {

    @Autowired
    private PaymentMapper mapper;

    @Configuration
    @ComponentScan(basePackages = "uk.gov.hmcts.opal.mapper")
    static class MapperTestConfig {
    }

    @Test
    public void testPaymentIsNull() {
        Payment payment = mapper.toDto(null);

        assertNull(payment);
    }

}
