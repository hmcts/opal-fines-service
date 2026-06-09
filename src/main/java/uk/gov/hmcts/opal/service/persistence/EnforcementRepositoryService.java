package uk.gov.hmcts.opal.service.persistence;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.common.exceptions.standard.InternalServerErrorException;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.EnforcementRepository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
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

    public Optional<EnforcementEntity> getEnforcementMostRecent(Long defendantAccountId) {
        return enforcementRepository.findTopByDefendantAccountIdOrderByPostedDateDescEnforcementIdDesc(
            defendantAccountId);
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
            cs.setString(4, RecordType.DEFENDANT_ACCOUNTS.toString());
            cs.setString(5, caseReference);
            cs.setString(6, functionCode);
            setNullableInteger(cs, 7, jailDays);
            cs.setString(8, postedBy);
            cs.setString(9, postedByName);
            cs.setString(10, reason);
            setNullableLong(cs, 11, enforcerId);
            cs.setObject(12, toJsonObject(resultResponses), Types.OTHER);
            setNullableTimestamp(cs, 13, earliestReleaseDate);
            cs.setLong(14, versionNumber);
            cs.registerOutParameter(15, Types.BIGINT);

            cs.execute();

            return cs.getLong(15);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to call stored procedure",
                                                   "p_add_defendant_account_enforcement", e);
        }
    }

    private void setNullableInteger(CallableStatement cs, int parameterIndex, Integer value) throws SQLException {
        if (value == null) {
            cs.setNull(parameterIndex, Types.INTEGER);
        } else {
            cs.setInt(parameterIndex, value);
        }
    }

    private void setNullableLong(CallableStatement cs, int parameterIndex, Long value) throws SQLException {
        if (value == null) {
            cs.setNull(parameterIndex, Types.BIGINT);
        } else {
            cs.setLong(parameterIndex, value);
        }
    }

    private void setNullableTimestamp(
        CallableStatement cs,
        int parameterIndex,
        LocalDateTime value
    ) throws SQLException {
        if (value == null) {
            cs.setNull(parameterIndex, Types.TIMESTAMP);
        } else {
            cs.setTimestamp(parameterIndex, Timestamp.valueOf(value));
        }
    }

    private PGobject toJsonObject(String value) throws SQLException {
        PGobject json = new PGobject();
        json.setType("json");
        json.setValue(value);
        return json;
    }
}
