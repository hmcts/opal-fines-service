package uk.gov.hmcts.opal.mapper.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;

public class CreditorAccountTypeMapperTest {

    private final CreditorAccountTypeMapper mapper = Mappers.getMapper(CreditorAccountTypeMapper.class);

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
