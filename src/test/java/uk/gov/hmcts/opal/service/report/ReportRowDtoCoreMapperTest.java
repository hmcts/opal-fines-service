package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.report.EnforcementReportRowDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.mapper.report.ReportRowDtoCoreMapper;

class ReportRowDtoCoreMapperTest {

    private final ReportRowDtoCoreMapper mapper = new ReportRowDtoCoreMapperImpl();
    private final ReportMetadataContext context = new ReportMetadataContext();


    @Test
    void shouldMapBasicFields() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        entity.setAccountNumber("ACC123");
        entity.setCollectionOrder(true);

        CourtEntity court = new CourtEntity();
        court.setName("Test Court");
        entity.setEnforcingCourt(court);

        EnforcementReportRowDto dto = mapper.map(entity, context);

        assertThat(dto.getHeader1()).isEqualTo("DETAIL");
        assertThat(dto.getAccountNo()).isEqualTo("ACC123");
        assertThat(dto.getImposingCourt()).isEqualTo("Test Court");
        assertThat(dto.getCollectionOrder()).isEqualTo("Y");
        //ignore fields that are set in the decorator
        assertThat(dto.getParentOrGuardian()).isNull();
        assertThat(dto.getLastEnforcementDate()).isNull();
    }

    @Test
    void shouldMapPartyFields() {
        PartyEntity party = new PartyEntity();
        party.setOrganisation(true);
        party.setOrganisationName("ACME Ltd");
        party.setAddressLine1("Line1");
        EnforcementReportRowDto dto = new EnforcementReportRowDto();

        mapper.mapParty(party, dto);

        assertThat(dto.getCompany()).isEqualTo("Y");
        assertThat(dto.getDefendantName()).isEqualTo("ACME Ltd");
        assertThat(dto.getAddress1()).isEqualTo("Line1");
    }

    @Test
    void shouldMapDebtorFields() {
        DebtorDetailEntity debtor = new DebtorDetailEntity();
        debtor.setVehicleRegistration("ABC123");
        EnforcementReportRowDto dto = new EnforcementReportRowDto();

        mapper.mapDebtor(debtor, dto);

        assertThat(dto.getVehicleReg()).isEqualTo("ABC123");
    }

    @Test
    void shouldBuildDefendantNameForPerson() {
        PartyEntity party = new PartyEntity();
        party.setOrganisation(false);
        party.setSurname("Smith");
        party.setForenames("John");

        String result = mapper.buildDefendantName(party);

        assertThat(result).isEqualTo("Smith, John");
    }

    @Test
    void shouldBuildDefendantNameForOrganisation() {
        PartyEntity party = new PartyEntity();
        party.setOrganisation(true);
        party.setOrganisationName("ACME Ltd");

        String result = mapper.buildDefendantName(party);

        assertThat(result).isEqualTo("ACME Ltd");
    }

    @Test
    void shouldTruncateTo34Chars() {
        String input = "1234567890123456789012345678901234567890";

        String result = mapper.truncate34(input);

        assertThat(result).hasSize(34);
    }

    @Test
    void shouldConvertBooleanToYesNo() {
        assertThat(mapper.booleanToYesNo(true)).isEqualTo("Y");
        assertThat(mapper.booleanToYesNo(false)).isEqualTo("N");
        assertThat(mapper.booleanToYesNo(null)).isNull();
    }
}