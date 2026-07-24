package uk.gov.hmcts.opal.entity.interfacejob;

public interface InterfaceJobStoredProcedureNames {

    String DB_PROC_NAME = "p_int_payments_in";
    String JPA_PROC_NAME = "InterfaceJob.ProcessPaymentsIn";
    String INTERFACE_JOB_ID = "pi_interface_job_id";
    String BUSINESS_UNIT_ID = "pi_business_unit_id";
    String POSTED_BY = "pi_posted_by";
    String POSTED_BY_NAME = "pi_posted_by_name";
    String TILL_ID = "po_till_id";
}
