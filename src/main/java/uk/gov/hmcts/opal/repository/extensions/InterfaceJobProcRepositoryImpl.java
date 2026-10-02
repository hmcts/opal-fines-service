package uk.gov.hmcts.opal.repository.extensions;

import static java.lang.String.format;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.BUSINESS_UNIT_ID;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.DB_PROC_NAME;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.INTERFACE_JOB_ID;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.POSTED_BY;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.POSTED_BY_NAME;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.RECORDS_JSON;
import static uk.gov.hmcts.opal.entity.interfacejob.InterfaceJobStoredProcedureNames.TILL_ID;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.repository.InterfaceJobProcRepository;

@Repository
@RequiredArgsConstructor
public class InterfaceJobProcRepositoryImpl implements InterfaceJobProcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long processPaymentsInJob(Long interfaceJobId, Short businessUnitId, String postedBy, String postedByName,
        String recordsJson)  {

        try {
            PGobject recordsJsonPgObject = new PGobject();
            recordsJsonPgObject.setType("json");
            recordsJsonPgObject.setValue(recordsJson);

            List<SqlParameter> params = List.of(
                new SqlParameter(INTERFACE_JOB_ID, Types.BIGINT),
                new SqlParameter(BUSINESS_UNIT_ID, Types.SMALLINT),
                new SqlParameter(POSTED_BY, Types.VARCHAR),
                new SqlParameter(POSTED_BY_NAME, Types.VARCHAR),
                new SqlParameter(RECORDS_JSON, Types.OTHER),
                new SqlOutParameter(TILL_ID, Types.BIGINT));

            Map<String, Object> resultMap = jdbcTemplate.call(con -> {
                String sql = format("call %s(?, ?, ?, ?, ?, ?)", DB_PROC_NAME);
                CallableStatement callableStatement = con.prepareCall(sql);
                callableStatement.registerOutParameter(6, Types.BIGINT);
                callableStatement.setLong(1, interfaceJobId);
                callableStatement.setShort(2, businessUnitId);
                callableStatement.setString(3, postedBy);
                callableStatement.setString(4, postedByName);
                callableStatement.setObject(5, recordsJsonPgObject);

                return callableStatement;
            }, params);

            return (Long) resultMap.get(TILL_ID);

        } catch(SQLException e) {
            throw new DataAccessException("Error setting " + RECORDS_JSON + " parameter", e){};
        }
    }
}
