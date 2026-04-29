package uk.gov.hmcts.opal.service.persistence;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.EnforcementRepository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j(topic = "opal.EnforcementRepositoryService")
@RequiredArgsConstructor
public class EnforcementRepositoryService {

    private final EnforcementRepository enforcementRepository;

    private final DataSource dataSource;

    @Transactional(readOnly = true)
    public Optional<EnforcementEntity> getEnforcementMostRecent(Long defendantAccountId, String lastEnforcement) {
        return enforcementRepository.findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
            defendantAccountId, lastEnforcement);
    }

    public Long addDefendantAccountEnforcement(
        String resultId,
        Long defendantAccountId,
        Short businessUnitId,
        String caseReference,
        String functionCode,
        Integer jailDays,
        String postedBy,
        String postedByName,
        String reason,
        Long enforcerId,
        String resultResponses,
        LocalDateTime earliestReleaseDate,
        Long versionNumber
    ) {

        String sql =
            "CALL p_add_defendant_account_enforcement(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS timestamp), ?, ?)";

        try (Connection connection = dataSource.getConnection();
             CallableStatement cs = connection.prepareCall(sql)) {

            cs.setString(1, resultId);
            cs.setLong(2, defendantAccountId);
            cs.setShort(3, businessUnitId);
            cs.setString(4, "defendant_accounts");
            cs.setString(5, caseReference);
            cs.setString(6, functionCode);

            if (jailDays != null) {
                cs.setInt(7, jailDays);
            } else {
                cs.setNull(7, Types.INTEGER);
            }

            cs.setString(8, postedBy);
            cs.setString(9, postedByName);
            cs.setString(10, reason);

            if (enforcerId != null) {
                cs.setLong(11, enforcerId);
            } else {
                cs.setNull(11, Types.BIGINT);
            }

            PGobject json = new PGobject();
            json.setType("json");
            json.setValue(resultResponses);
            cs.setObject(12, json, Types.OTHER);

            if (earliestReleaseDate != null) {
                cs.setTimestamp(13, Timestamp.valueOf(earliestReleaseDate));
            } else {
                cs.setNull(13, Types.TIMESTAMP);
            }

            cs.setLong(14, versionNumber);
            cs.registerOutParameter(15, Types.BIGINT);

            System.out.println(cs);

            cs.execute();

            return cs.getLong(15);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call p_add_defendant_account_enforcement", e);
        }
    }
}
