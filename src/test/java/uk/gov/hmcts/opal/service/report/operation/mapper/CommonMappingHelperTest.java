package uk.gov.hmcts.opal.service.report.operation.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.service.report.CommonReportStringConstants;

class CommonMappingHelperTest {

    public static final String SURNAME = "Smith";
    public static final String LONGER_THAN_34_CHARS = "12345678901234567890123456789012345";
    public static final String ORGANISATION = "Organisation 1";
    private final CommonMappingHelper helper = new CommonMappingHelper() {
    };

    @Test
    void buildDefendantName_whenOrganisation_returnsOrganisationName() {
        PartyEntity party = mock(PartyEntity.class);
        when(party.isOrganisation()).thenReturn(true);
        when(party.getOrganisationName()).thenReturn(ORGANISATION);

        assertThat(helper.buildDefendantName(party)).isEqualTo(ORGANISATION);
    }

    @Test
    void buildDefendantName_whenIndividualWithForenames_returnsSurnameAndForenames() {
        PartyEntity party = mock(PartyEntity.class);
        when(party.isOrganisation()).thenReturn(false);
        when(party.getSurname()).thenReturn(SURNAME);
        when(party.getForenames()).thenReturn("John");

        assertThat(helper.buildDefendantName(party)).isEqualTo("Smith, John");
    }

    @Test
    void buildDefendantName_whenIndividualWithoutForenames_returnsSurnameOnly() {
        PartyEntity party = mock(PartyEntity.class);
        when(party.isOrganisation()).thenReturn(false);
        when(party.getSurname()).thenReturn(SURNAME);
        when(party.getForenames()).thenReturn(null);

        assertThat(helper.buildDefendantName(party)).isEqualTo(SURNAME);
    }

    @Test
    void buildDefendantName_whenForenamesBlank_returnsSurnameOnly() {
        PartyEntity party = mock(PartyEntity.class);
        when(party.isOrganisation()).thenReturn(false);
        when(party.getSurname()).thenReturn(SURNAME);
        when(party.getForenames()).thenReturn("   ");

        assertThat(helper.buildDefendantName(party)).isEqualTo(SURNAME);
    }

    @Test
    void truncate34_whenValueLongerThan34_truncatesTo34Chars() {
        String value = LONGER_THAN_34_CHARS;
        assertThat(value).hasSizeGreaterThan(34);
        assertThat(helper.truncate34(value)).hasSize(34);

    }

    @Test
    void truncate34_whenValueShorterThanOrEqualTo34_returnsSameValue() {
        String value = "1234567890";
        assertThat(helper.truncate34(value)).isEqualTo(value);
    }

    @Test
    void truncate34_whenValueIsNull_returnsNull() {
        assertThat(helper.truncate34(null)).isNull();
    }

    @Test
    void booleanToYesNo_whenTrue_returnsY() {
        assertThat(helper.booleanToYesNo(true)).isEqualTo(CommonReportStringConstants.YES);
    }

    @Test
    void booleanToYesNo_whenFalse_returnsN() {
        assertThat(helper.booleanToYesNo(false)).isEqualTo(CommonReportStringConstants.NO);
    }

    @Test
    void booleanToYesNo_whenNull_returnsNull() {
        assertThat(helper.booleanToYesNo(null)).isNull();
    }

    @ParameterizedTest
    @CsvSource({"true, Y", "false, N"})
    void organisationToYesNo_returnsExpectedValue(boolean isOrganisation, String expected) {
        PartyEntity party = mock(PartyEntity.class);
        given(party.isOrganisation()).willReturn(isOrganisation);
        String result = helper.organisationToYesNo(party);
        assertThat(result).isEqualTo(expected);
    }
}