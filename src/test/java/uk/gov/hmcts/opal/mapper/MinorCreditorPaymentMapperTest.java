package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditorPayment;

@SpringJUnitConfig
@ContextConfiguration(classes = {MinorCreditorPaymentMapperImpl.class})
class MinorCreditorPaymentMapperTest {

    @Autowired
    private MinorCreditorPaymentMapper mapper;

    @Test
    void givenCreditorAccount_whenToMinorCreditorPayment_thenMapsPaymentFields() {
        CreditorAccountEntity.Lite account = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(101L)
            .creditorAccountType(CreditorAccountType.MN)
            .bankAccountName("A NAME")
            .bankSortCode("112233")
            .bankAccountNumber("12345678")
            .bankAccountReference("REF")
            .payByBacs(true)
            .holdPayout(false)
            .build();

        MinorCreditorAccountResponseMinorCreditorPayment mapped = mapper.toMinorCreditorPayment(account);

        assertNotNull(mapped);
        assertEquals("A NAME", mapped.getAccountName());
        assertEquals("112233", mapped.getSortCode());
        assertEquals("12345678", mapped.getAccountNumber());
        assertEquals("REF", mapped.getAccountReference());
        assertEquals(true, mapped.getPayByBacs());
        assertEquals(false, mapped.getHoldPayment());
    }
}
