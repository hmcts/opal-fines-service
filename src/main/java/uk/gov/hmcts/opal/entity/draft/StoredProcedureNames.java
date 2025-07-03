package uk.gov.hmcts.opal.entity.draft;

public interface StoredProcedureNames {
    String DB_PROC_NAME = "p_stub_create_defendant_account";
    String JPA_PROC_NAME = "DraftAccount.Publish";
    String DRAFT_ACC_ID = "pi_draft_account_id";
    String BUSINESS_UNIT_ID = "pi_business_unit_id";
    String POSTED_BY = "pi_posted_by";
    String POSTED_BY_NAME = "pi_posted_by_name";
    String DEF_ACC_NO = "pio_account_number";
    String DEF_ACC_ID = "pio_defendant_account_id";
}
