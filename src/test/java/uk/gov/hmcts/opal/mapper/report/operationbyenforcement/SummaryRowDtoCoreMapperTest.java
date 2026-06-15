package uk.gov.hmcts.opal.mapper.report.operationbyenforcement;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportRowDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper.SummaryRowDtoCoreMapper;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper.SummaryRowDtoCoreMapperImpl;

class SummaryRowDtoCoreMapperTest {

    public static final String ACCOUNT_NUMBER = "ACC123";
    public static final String COURT_NAME = "Test Court";
    public static final String Y = "Y";
    public static final String N = "N";
    public static final String ORGANISATION_NAME = "Organisation";
    public static final String VEHICLE_REGISTRATION = "VEH123";
    public static final String ADDRESS_LINE_1 = "Line1";
    private final SummaryRowDtoCoreMapper mapper = new SummaryRowDtoCoreMapperImpl();
    private final ReportMetadataContext context = new ReportMetadataContext();


    @Test
    void shouldMapBasicFields() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        entity.setAccountNumber(ACCOUNT_NUMBER);
        entity.setCollectionOrder(true);

        CourtEntity court = new CourtEntity();
        court.setName(COURT_NAME);
        entity.setEnforcingCourt(court);

        OperationByEnforcementSummaryReportRowDto dto = mapper.map(entity, context);

        assertThat(dto.getHeader1()).isEqualTo("DETAIL");
        assertThat(dto.getAccountNo()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(dto.getImposingCourt()).isEqualTo(COURT_NAME);
        assertThat(dto.getCollectionOrder()).isEqualTo(Y);
        //ignore fields that are set in the decorator
        assertThat(dto.getParentOrGuardian()).isNull();
        assertThat(dto.getLastEnforcementDate()).isNull();
    }

    @Test
    void shouldMapPartyFields() {
        PartyEntity party = new PartyEntity();
        party.setOrganisation(true);
        party.setOrganisationName(ORGANISATION_NAME);
        party.setAddressLine1(ADDRESS_LINE_1);
        OperationByEnforcementSummaryReportRowDto
            dto = new OperationByEnforcementSummaryReportRowDto();

        mapper.mapParty(party, dto);

        assertThat(dto.getCompany()).isEqualTo(Y);
        assertThat(dto.getDefendantName()).isEqualTo(ORGANISATION_NAME);
        assertThat(dto.getAddress1()).isEqualTo(ADDRESS_LINE_1);
    }

    @Test
    void shouldMapDebtorFields() {
        DebtorDetailEntity debtor = new DebtorDetailEntity();
        debtor.setVehicleRegistration(VEHICLE_REGISTRATION);
        OperationByEnforcementSummaryReportRowDto
            dto = new OperationByEnforcementSummaryReportRowDto();

        mapper.mapDebtor(debtor, dto);

        assertThat(dto.getVehicleReg()).isEqualTo(VEHICLE_REGISTRATION);
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
        party.setOrganisationName(ORGANISATION_NAME);

        String result = mapper.buildDefendantName(party);

        assertThat(result).isEqualTo(ORGANISATION_NAME);
    }

    @Test
    void shouldTruncateTo34Chars() {
        String input = "1234567890123456789012345678901234567890";
        String result = mapper.truncate34(input);
        assertThat(input).hasSizeGreaterThan(34);
        assertThat(result).hasSize(34);
    }

    @Test
    void shouldConvertBooleanToYesNo() {
        assertThat(mapper.booleanToYesNo(true)).isEqualTo(Y);
        assertThat(mapper.booleanToYesNo(false)).isEqualTo(N);
        assertThat(mapper.booleanToYesNo(null)).isNull();
    }
}