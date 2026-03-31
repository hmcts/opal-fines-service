package uk.gov.hmcts.opal.mapper.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.mapper.common.CreditorAccountTypeMapperTest.TestConfig;

@SpringJUnitConfig(classes = TestConfig.class)
public class CreditorAccountTypeMapperTest {

    @Configuration
    @ComponentScan(basePackageClasses = CreditorAccountTypeMapper.class)
    static class TestConfig {

    }

    @Autowired
    private CreditorAccountTypeMapper mapper;

    @Test
    void shouldMapMinorCreditor() {
        //Arrange
        CreditorAccountType entity = CreditorAccountType.MN;

        //Act
        CreditorAccountTypeReference mapped = mapper.toDto(entity);

        //Assert
        assertEquals("MN", mapped.getType());
        assertEquals("Minor Creditor", mapped.getDisplayName());
    }

    @Test
    void shouldMapMajorCreditor() {
        //Arrange
        CreditorAccountType entity = CreditorAccountType.MJ;

        //Act
        CreditorAccountTypeReference mapped = mapper.toDto(entity);

        //Assert
        assertEquals("MJ", mapped.getType());
        assertEquals("Major Creditor", mapped.getDisplayName());
    }

    @Test
    void shouldMapCentralFund() {
        //Arrange
        CreditorAccountType entity = CreditorAccountType.CF;

        //Act
        CreditorAccountTypeReference mapped = mapper.toDto(entity);

        //Assert
        assertEquals("CF", mapped.getType());
        assertEquals("Central Fund", mapped.getDisplayName());
    }
}
