package uk.gov.hmcts.opal.steps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseStepDef {
    static Logger log = LoggerFactory.getLogger(DatabaseStepDef.class.getName());
    private DBConnection db;
    private Connection conn;
    private PreparedStatement pstmt = null;

    public JSONArray getCourtsByCourtName(String courtName) throws SQLException, JSONException {
        db = new DBConnection();
        conn = db.getPostgresConnection();
        List<JSONObject> results = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(
                "SELECT court_id, business_unit_id, court_code, name FROM courts WHERE name like '%"
                    + courtName + "%'");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                log.info("Court ID: " + rs.getString("court_id"));
                log.info("Business Unit ID: " + rs.getString("business_unit_id"));
                log.info("Court Code: " + rs.getString("court_code"));
                log.info("Court Name: " + rs.getString("name"));

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("court_id", rs.getString("court_id"));
                jsonObject.put("business_unit_id", rs.getString("business_unit_id"));
                jsonObject.put("court_code", rs.getString("court_code"));
                jsonObject.put("court_name", rs.getString("name"));

                results.add(jsonObject);
            }

            log.info("Results: " + results);

        } catch (SQLException e) {
            log.error("SQL error: " + e.getMessage());
        } catch (JSONException e) {
            log.error("JSON error: " + e.getMessage());
            throw new JSONException(e.toString());
        } finally {
            pstmt.close();
            conn.close();
        }
        return new JSONArray(results.toString());
    }

    public JSONArray getMajorCredByID(String id) throws SQLException, JSONException {
        db = new DBConnection();
        conn = db.getPostgresConnection();
        List<JSONObject> results = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(
                "select major_creditor_id, business_unit_id, major_creditor_code, name "
                    + "from major_creditors where major_creditor_id = '"
                    + id + "'");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {

                log.info("Major Creditor ID: " + rs.getString("major_creditor_id"));
                log.info("Business Unit ID: " + rs.getString("business_unit_id"));
                log.info("Major Creditor Code: " + rs.getString("major_creditor_code"));
                log.info("Major Creditor Name: " + rs.getString("name"));

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("major_creditor_id", rs.getString("major_creditor_id"));
                jsonObject.put("business_unit_id", rs.getString("business_unit_id"));
                jsonObject.put("major_creditor_code", rs.getString("major_creditor_code"));
                jsonObject.put("major_creditor_name", rs.getString("name"));

                results.add(jsonObject);
            }

            log.info("Results: " + results);

        } catch (SQLException e) {
            log.error("SQL error: " + e.getMessage());
        } catch (JSONException e) {
            log.error("JSON error: " + e.getMessage());
            throw new JSONException(e.toString());
        } finally {
            try {
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                log.error("SQL error: " + e.getMessage());
            }
        }
        return new JSONArray(results.toString());
    }
}
