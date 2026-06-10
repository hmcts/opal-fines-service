package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse.MajorCreditorAddressLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse.MajorCreditorLegacy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = GetMajorCreditorAccountAtAGlanceResponseLegacyMapperTest.MapperTestConfig.class)
@Isolated
class GetMajorCreditorAccountAtAGlanceResponseLegacyMapperTest {

    @Autowired
    private GetMajorCreditorAccountAtAGlanceResponseLegacyMapper mapper;

    @Test
    void toOpal_mapsFullLegacyResponse() {
        GetMajorCreditorAccountAtAGlanceLegacyResponse legacy =
            GetMajorCreditorAccountAtAGlanceLegacyResponse.builder()
                .majorCreditor(MajorCreditorLegacy.builder()
                    .creditorAccountId(99000000000800L)
                    .creditorAccountVersion(BigInteger.valueOf(7))
                    .name("Major Creditor Test Ltd")
                    .code("MC01")
                    .payByBacs(Boolean.TRUE)
                    .address(MajorCreditorAddressLegacy.builder()
                        .line1("1 Test Street")
                        .line2("London")
                        .line3("NW1")
                        .postcode("NW1 1AA")
                        .build())
                    .build())
                .build();

        GetMajorCreditorAccountAtAGlanceResponse response = mapper.toOpal(legacy);

        assertNotNull(response);
        assertEquals(99000000000800L, response.getMajorCreditor().getCreditorAccountId());
        assertEquals("Major Creditor Test Ltd", response.getMajorCreditor().getName());
        assertEquals("MC01", response.getMajorCreditor().getCode());
        assertEquals(Boolean.TRUE, response.getMajorCreditor().getPayByBacs());
        assertNotNull(response.getMajorCreditor().getAddress());
        assertEquals("1 Test Street", response.getMajorCreditor().getAddress().getLine1());
        assertEquals("London", response.getMajorCreditor().getAddress().getLine2());
        assertEquals("NW1", response.getMajorCreditor().getAddress().getLine3());
        assertEquals("NW1 1AA", response.getMajorCreditor().getAddress().getPostcode());
    }

    @Test
    void toOpal_mapsNullOptionalFields() {
        GetMajorCreditorAccountAtAGlanceLegacyResponse legacy =
            GetMajorCreditorAccountAtAGlanceLegacyResponse.builder()
                .majorCreditor(MajorCreditorLegacy.builder()
                    .creditorAccountId(123L)
                    .creditorAccountVersion(BigInteger.ONE)
                    .name("Central Fund")
                    .build())
                .build();

        GetMajorCreditorAccountAtAGlanceResponse response = mapper.toOpal(legacy);

        assertNotNull(response);
        assertEquals(123L, response.getMajorCreditor().getCreditorAccountId());
        assertEquals("Central Fund", response.getMajorCreditor().getName());
        assertNull(response.getMajorCreditor().getCode());
        assertNull(response.getMajorCreditor().getAddress());
        assertNull(response.getMajorCreditor().getPayByBacs());
    }

    @Configuration
    @ComponentScan(basePackages = "uk.gov.hmcts.opal.mapper")
    static class MapperTestConfig {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
