package uk.gov.hmcts.opal.entity.interfacemessage;

public interface InterfaceMessageStoredProcedureNames {

    String DB_PROC_NAME = "p_insert_interface_message";
    String JPA_PROC_NAME = "InterfaceMessage.Insert";
    String INTERFACE_JOB_ID = "pi_interface_job_id";
    String MESSAGE_TYPE = "pi_message_type";
    String MESSAGE_TEXT = "pi_message_text";
    String INTERFACE_FILE_ID = "pi_interface_file_id";
    String RECORD_INDEX = "pi_record_index";
    String RECORD_DETAIL = "pi_record_detail";
    String MESSAGE_DATA = "pi_message_data";
}
