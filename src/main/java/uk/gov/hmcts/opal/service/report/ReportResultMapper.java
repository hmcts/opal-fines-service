package uk.gov.hmcts.opal.service.report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;

/**
 * Maps domain entities to report DTO rows and wraps them in a transaction + metadata object.
 */
@Component
@RequiredArgsConstructor
public class ReportResultMapper {

    private final ReportRowDtoMapper rowMapper;
    private final ReportEnrichmentService enrichment;

    public OperationReportByEnforcementTransaction map(List<DefendantAccountEntity> accounts) {
        // map each account to an EnforcementReportRowDto
        List<EnforcementReportRowDto> rows = accounts.stream()
            .map(a -> rowMapper.map(a, enrichment))
            .collect(Collectors.toList());

        OperationReportByEnforcementTransaction report = new OperationReportByEnforcementTransaction();
        report.setTransactionList(rows);

        List<ParticipantIdentifier> participants = new ArrayList<>();

        for (EnforcementReportRowDto row : rows) {
            if (row == null) {
                continue;
            }

            // party name (formatted defname)
            if (row.getDefname() != null && !row.getDefname().isBlank()) {
                participants.add(ParticipantIdentifier.builder()
                    .identifier(row.getDefname())
                    .type(PdplIdentifierType.PARTY_NAME)
                    .build());
            }

            // national insurance number (nino)
            if (row.getNino() != null && !row.getNino().isBlank()) {
                participants.add(ParticipantIdentifier.builder()
                    .identifier(row.getNino())
                    .type(PdplIdentifierType.NINO)
                    .build());
            }

            // date of birth
            if (row.getDob() != null) {
                participants.add(ParticipantIdentifier.builder()
                    .identifier(row.getDob().toString()) // ISO format
                    .type(PdplIdentifierType.DOB)
                    .build());
            }

            // account number
            if (row.getAccountNo() != null && !row.getAccountNo().isBlank()) {
                participants.add(ParticipantIdentifier.builder()
                    .identifier(row.getAccountNo())
                    .type(PdplIdentifierType.ACCOUNT_NUMBER)
                    .build());
            }

            if (row.getCompany() != null && !row.getCompany().isBlank()) {
                participants.add(ParticipantIdentifier.builder()
                    .identifier(row.getCompany())
                    .type(PdplIdentifierType.ORGANISATION)
                    .build());
            }

            // warrant reference
            if (row.getWarrNo() != null && !row.getWarrNo().isBlank()) {
                participants.add(ParticipantIdentifier.builder()
                    .identifier(row.getWarrNo())
                    .type(PdplIdentifierType.WARRANT_REFERENCE)
                    .build());
            }

            // prosecutor case reference
            if (row.getPcr() != null && !row.getPcr().isBlank()) {
                participants.add(ParticipantIdentifier.builder()
                    .identifier(row.getPcr())
                    .type(PdplIdentifierType.PROSECUTOR_CASE_REFERENCE)
                    .build());
            }

        }

        // attach metadata to the report
        ReportMetaData meta = new ReportMetaData();
        meta.setPdpoParticipants(participants);
        report.setReportMetaData(meta);

        // return the report (transaction list + metadata)
        return report;
    }
}