package uk.gov.hmcts.opal.entity.defendantaccount;

public interface StoredProcedureNames {

    //Defendant Account Enforcement Stored Procedure Names
    String ADD_ENFORCEMENT_PROC = "p_add_defendant_account_enforcement";
    String RESULT_ID = "pi_result_id";
    String DEFENDANT_ID = "pi_defendant_account_id";
    String BUSINESS_UNIT_ID = "pi_business_unit_id";
    String RECORD_TYPE = "pi_record_type";
    String CASE_REFERENCE = "pi_case_reference";
    String FUNCTION_CODE = "pi_function_code";
    String JAIL_DAYS = "pi_jail_days";
    String POSTED_BY = "pi_posted_by";
    String POSTED_BY_NAME = "pi_posted_by_name";
    String REASON =  "pi_reason";
    String ENFORCER_ID = "pi_enforcer_id";
    String RESULT_RESPONSES = "pi_result_responses";
    String EARLIEST_RELEASE_DATE = "pi_earliest_release_date";
    String VERSION_NUMBER = "pi_version_number";
}
