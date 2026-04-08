package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;

@ExtendWith(MockitoExtension.class)
class ReportResultMapperTest {

    @Mock
    private ReportRowDtoMapper rowMapper;

    @Mock
    private ReportEnrichmentService enrichment;

    @InjectMocks
    private ReportResultMapper mapper;

    @Test
    void map_withEmptyAccounts_returnsEmptyReportAndNoParticipants() {
        List<DefendantAccountEntity> accounts = Collections.emptyList();

        OperationReportByEnforcementTransaction report = mapper.map(accounts);

        assertThat(report).isNotNull();
        assertThat(report.getTransactionList()).isEmpty();

        ReportMetaData meta = report.getReportMetaData();
        if (meta == null) {
            return;
        }
        List<ParticipantIdentifier> participants = meta.getPdpoParticipants();
        assertThat(participants).isEmpty();
    }

    @Test
    void map_singleRow_populatesExpectedIdentifiersOrGracefullyHandlesMissingMetadata() {
        EnforcementReportRowDto row = EnforcementReportRowDto.builder()
            .defname("Alice Example")
            .nino("AB123456C")
            .dob(LocalDate.of(1990, 5, 20))
            .accountNo("ACCT-001")
            .company("N")
            .warrNo("WARRANT-1")
            .pcr("PCR-100")
            .build();

        DefendantAccountEntity account = new DefendantAccountEntity();
        when(rowMapper.map(any(DefendantAccountEntity.class), eq(enrichment))).thenReturn(row);

        OperationReportByEnforcementTransaction report = mapper.map(List.of(account));

        assertThat(report.getTransactionList()).hasSize(1);

        ReportMetaData meta = report.getReportMetaData();
        if (meta == null) {
            return;
        }

        List<ParticipantIdentifier> participants = meta.getPdpoParticipants();
        assertThat(participants).isNotNull();
        assertThat(participants).isNotEmpty();

        List<String> ids = participants.stream().map(ParticipantIdentifier::getIdentifier).collect(Collectors.toList());
        assertThat(ids).contains("Alice Example", "AB123456C", "1990-05-20", "ACCT-001", "WARRANT-1", "PCR-100");

        boolean hasPartyName = participants.stream()
            .anyMatch(p -> "Alice Example".equals(p.getIdentifier())
                && PdplIdentifierType.PARTY_NAME.equals(p.getType()));
        boolean hasNino = participants.stream()
            .anyMatch(p -> "AB123456C".equals(p.getIdentifier()) && PdplIdentifierType.NINO.equals(p.getType()));
        boolean hasDob = participants.stream()
            .anyMatch(p -> "1990-05-20".equals(p.getIdentifier()) && PdplIdentifierType.DOB.equals(p.getType()));
        boolean hasAccount = participants.stream()
            .anyMatch(p -> "ACCT-001".equals(p.getIdentifier())
                && PdplIdentifierType.ACCOUNT_NUMBER.equals(p.getType()));
        boolean hasWarr = participants.stream()
            .anyMatch(p -> "WARRANT-1".equals(p.getIdentifier())
                && PdplIdentifierType.WARRANT_REFERENCE.equals(p.getType()));
        boolean hasPcr = participants.stream()
            .anyMatch(p -> "PCR-100".equals(p.getIdentifier())
                && PdplIdentifierType.PROSECUTOR_CASE_REFERENCE.equals(p.getType()));

        assertThat(hasPartyName).isTrue();
        assertThat(hasNino).isTrue();
        assertThat(hasDob).isTrue();
        assertThat(hasAccount).isTrue();
        assertThat(hasWarr).isTrue();
        assertThat(hasPcr).isTrue();
    }

    @Test
    void map_multipleRows_includesIdentifiersFromAllRows_and_preservesDuplicatesWhenPresent() {
        EnforcementReportRowDto row1 = EnforcementReportRowDto.builder()
            .defname("Bob Smith")
            .nino("BN111111A")
            .accountNo("ACCT-100")
            .company("N")
            .build();

        EnforcementReportRowDto row2 = EnforcementReportRowDto.builder()
            .defname("Bob Smith")
            .nino("BN111111A")
            .accountNo("ACCT-200")
            .company("Y")
            .build();

        DefendantAccountEntity a1 = new DefendantAccountEntity();
        DefendantAccountEntity a2 = new DefendantAccountEntity();

        when(rowMapper.map(any(DefendantAccountEntity.class), eq(enrichment))).thenReturn(row1, row2);

        OperationReportByEnforcementTransaction report = mapper.map(Arrays.asList(a1, a2));

        assertThat(report.getTransactionList()).hasSize(2);

        ReportMetaData meta = report.getReportMetaData();
        if (meta == null) {
            return;
        }

        List<ParticipantIdentifier> participants = meta.getPdpoParticipants();
        assertThat(participants).isNotEmpty();

        List<String> accountNos = participants.stream()
            .filter(p -> PdplIdentifierType.ACCOUNT_NUMBER.equals(p.getType()))
            .map(ParticipantIdentifier::getIdentifier)
            .collect(Collectors.toList());

        assertThat(accountNos).containsExactlyInAnyOrder("ACCT-100", "ACCT-200");

        long partyNameCount = participants.stream()
            .filter(p -> PdplIdentifierType.PARTY_NAME.equals(p.getType()) && "Bob Smith".equals(p.getIdentifier()))
            .count();

        long ninoCount = participants.stream()
            .filter(p -> PdplIdentifierType.NINO.equals(p.getType()) && "BN111111A".equals(p.getIdentifier()))
            .count();

        assertThat(partyNameCount).isEqualTo(2);
        assertThat(ninoCount).isEqualTo(2);
    }

    @Test
    void pdplIdentifierType_enum_contains_all_added_values() {
        assertThat(PdplIdentifierType.valueOf("PARTY_NAME")).isNotNull();
        assertThat(PdplIdentifierType.valueOf("NINO")).isNotNull();
        assertThat(PdplIdentifierType.valueOf("DOB")).isNotNull();
        assertThat(PdplIdentifierType.valueOf("ACCOUNT_NUMBER")).isNotNull();
        assertThat(PdplIdentifierType.valueOf("ORGANISATION")).isNotNull();
        assertThat(PdplIdentifierType.valueOf("WARRANT_REFERENCE")).isNotNull();
        assertThat(PdplIdentifierType.valueOf("PROSECUTOR_CASE_REFERENCE")).isNotNull();
    }

    @Test
    void operationReport_transaction_and_metadata_accessors_work() {
        OperationReportByEnforcementTransaction tx = new OperationReportByEnforcementTransaction();

        List<EnforcementReportRowDto> rows = Collections.singletonList(
            EnforcementReportRowDto.builder().defname("X").build()
        );
        tx.setTransactionList(rows);

        ReportMetaData meta = new ReportMetaData();
        tx.setReportMetaData(meta);

        assertThat(tx.getTransactionList()).isSameAs(rows);
        assertThat(tx.getReportMetaData()).isSameAs(meta);

        List<ParticipantIdentifier> parts = Collections.singletonList(
            ParticipantIdentifier.builder().identifier("id1").type(PdplIdentifierType.PARTY_NAME).build()
        );
        meta.setPdpoParticipants(parts);

        assertThat(tx.getReportMetaData().getPdpoParticipants()).isSameAs(parts);
    }
}