/**
* OPAL Program
*
* MODULE      : insert_notes.sql
*
* DESCRIPTION : Inserts two rows of data into the DEFENDANT_ACCOUNTS table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 17/11/2023    T REED       1.0         Inserts two rows of data into the DEFENDANT_ACCOUNTS table
*
**/

INSERT INTO defendant_accounts (
    defendant_account_id,
    business_unit_id,
    account_number,
    imposed_hearing_date,
    imposing_court_id,
    amount_imposed,
    amount_paid,
    account_balance,
    account_status,
    completed_date,
    enforcing_court_id,
    last_hearing_court_id,
    last_hearing_date,
    last_movement_date,
    last_enforcement,
    last_changed_date,
    originator_name,
    originator_reference,
    originator_type,
    allow_writeoffs,
    allow_cheques,
    cheque_clearance_period,
    credit_transfer_clearance_period,
    enforcement_override_result_id,
    enforcement_override_enforcer_id,
    enforcement_override_tfo_lja_id,
    unit_fine_detail,
    unit_fine_value,
    collection_order,
    collection_order_effective_date,
    further_steps_notice_date,
    confiscation_order_date,
    fine_registration_date,
    suspended_committal_enforcement_id,
    consolidated_account_type,
    payment_card_requested,
    payment_card_requested_date,
    payment_card_requested_by,
    prosecutor_case_reference,
    enforcement_case_status
)
VALUES
    (nextval('defendant_account_id_seq'), 1, 'TESTDATA123', '2023-11-09 00:00:00', 1, 500.00, 100.00, 400.00, 'L', '2023-12-09 00:00:00', 1, 1, '2023-11-09 00:00:00', '2023-11-09 00:00:00', 'ENF001', '2023-11-09 00:00:00', 'Court Name', 'Ref123', 'Type1', TRUE, TRUE, 7, 7, 'OVR001', 1, 1, 'Fine Details', 50.00, TRUE, '2023-11-09 00:00:00', '2023-11-09 00:00:00', '2023-11-09 00:00:00', '2023-11-09 00:00:00', 1, 'M', FALSE, '2023-11-09 00:00:00', 'User123', 'CaseRef123', 'Active'),
    (nextval('defendant_account_id_seq'), 2, 'TESTDATA456', '2023-11-10 00:00:00', 2, 1000.00, 200.00, 800.00, 'L', '2023-12-10 00:00:00', 2, 2, '2023-11-10 00:00:00', '2023-11-10 00:00:00', 'ENF002', '2023-11-10 00:00:00', 'Another Court', 'Ref456', 'Type2', TRUE, TRUE, 7, 7, 'OVR002', 2, 2, 'More Fine Details', 100.00, TRUE, '2023-11-10 00:00:00', '2023-11-10 00:00:00', '2023-11-10 00:00:00', '2023-11-10 00:00:00', 2, 'C', TRUE, '2023-11-10 00:00:00', 'User456', 'CaseRef456', 'Pending');
