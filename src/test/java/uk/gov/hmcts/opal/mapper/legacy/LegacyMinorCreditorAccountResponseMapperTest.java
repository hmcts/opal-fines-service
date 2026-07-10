package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationAliasCommon;

class LegacyMinorCreditorAccountResponseMapperTest {

    private final LegacyMinorCreditorAccountResponseMapper mapper =
        Mappers.getMapper(LegacyMinorCreditorAccountResponseMapper.class);

    @Test
    void toIndividualDetailsCommon_returnsNull_whenLegacyIndividualDetailsIsNull() {
        LegacyPartyDetails legacy = LegacyPartyDetails.builder()
            .organisationFlag(false)
            .individualDetails(null)
            .build();

        assertNull(mapper.toIndividualDetailsCommon(legacy));
    }

    @Test
    void toIndividualDetailsCommon_mapsFieldsAndAliases() {
        IndividualDetails.IndividualAlias firstAlias = IndividualDetails.IndividualAlias.builder()
            .aliasId("IA1")
            .sequenceNumber((short) 1)
            .surname("AliasSurname1")
            .forenames("AliasForenames1")
            .build();

        IndividualDetails.IndividualAlias secondAlias = IndividualDetails.IndividualAlias.builder()
            .aliasId("IA2")
            .sequenceNumber(null)
            .surname("AliasSurname2")
            .forenames("AliasForenames2")
            .build();

        LegacyPartyDetails legacy = LegacyPartyDetails.builder()
            .organisationFlag(false)
            .individualDetails(IndividualDetails.builder()
                .title("Ms")
                .forenames("Jane Mary")
                .surname("Smith")
                .dateOfBirth(LocalDate.of(1990, 5, 21))
                .age("34")
                .nationalInsuranceNumber("AB123456C")
                .individualAliases(new IndividualDetails.IndividualAlias[] {firstAlias, secondAlias})
                .build())
            .build();

        IndividualDetailsCommon result = mapper.toIndividualDetailsCommon(legacy);

        assertNotNull(result);
        assertEquals("Ms", result.getTitle());
        assertEquals("Jane Mary", result.getForenames());
        assertEquals("Smith", result.getSurname());
        assertEquals("1990-05-21", result.getDateOfBirth());
        assertEquals("34", result.getAge());
        assertEquals("AB123456C", result.getNationalInsuranceNumber());

        List<IndividualAliasCommon> aliases = result.getIndividualAliases();
        assertNotNull(aliases);
        assertEquals(2, aliases.size());
        assertEquals("IA1", aliases.get(0).getAliasId());
        assertEquals(1, aliases.get(0).getSequenceNumber());
        assertEquals("AliasSurname1", aliases.get(0).getSurname());
        assertEquals("AliasForenames1", aliases.get(0).getForenames());
        assertEquals("IA2", aliases.get(1).getAliasId());
        assertNull(aliases.get(1).getSequenceNumber());
        assertEquals("AliasSurname2", aliases.get(1).getSurname());
        assertEquals("AliasForenames2", aliases.get(1).getForenames());
    }

    @Test
    void toOrganisationAliases_returnsNull_whenAliasesAreNull() {
        assertNull(mapper.toOrganisationAliases(null));
    }

    @Test
    void toOrganisationAliases_mapsAliasFields() {
        OrganisationDetails.OrganisationAlias firstAlias = OrganisationDetails.OrganisationAlias.builder()
            .aliasId("OA1")
            .sequenceNumber((short) 3)
            .organisationName("Org Alias One")
            .build();

        OrganisationDetails.OrganisationAlias secondAlias = OrganisationDetails.OrganisationAlias.builder()
            .aliasId("OA2")
            .sequenceNumber(null)
            .organisationName("Org Alias Two")
            .build();

        List<OrganisationAliasCommon> result =
            mapper.toOrganisationAliases(new OrganisationDetails.OrganisationAlias[] {firstAlias, secondAlias});

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("OA1", result.get(0).getAliasId());
        assertEquals(3, result.get(0).getSequenceNumber());
        assertEquals("Org Alias One", result.get(0).getOrganisationName());
        assertEquals("OA2", result.get(1).getAliasId());
        assertNull(result.get(1).getSequenceNumber());
        assertEquals("Org Alias Two", result.get(1).getOrganisationName());
    }

    @Test
    void toIndividualAliases_returnsNull_whenAliasesAreNull() {
        assertNull(mapper.toIndividualAliases(null));
    }

    @Test
    void toIndividualAliases_mapsAliasFields() {
        IndividualDetails.IndividualAlias firstAlias = IndividualDetails.IndividualAlias.builder()
            .aliasId("IA3")
            .sequenceNumber((short) 4)
            .surname("Brown")
            .forenames("Pat")
            .build();

        IndividualDetails.IndividualAlias secondAlias = IndividualDetails.IndividualAlias.builder()
            .aliasId("IA4")
            .sequenceNumber(null)
            .surname("Green")
            .forenames("Sam")
            .build();

        List<IndividualAliasCommon> result =
            mapper.toIndividualAliases(new IndividualDetails.IndividualAlias[] {firstAlias, secondAlias});

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("IA3", result.get(0).getAliasId());
        assertEquals(4, result.get(0).getSequenceNumber());
        assertEquals("Brown", result.get(0).getSurname());
        assertEquals("Pat", result.get(0).getForenames());
        assertEquals("IA4", result.get(1).getAliasId());
        assertNull(result.get(1).getSequenceNumber());
        assertEquals("Green", result.get(1).getSurname());
        assertEquals("Sam", result.get(1).getForenames());
    }
}
