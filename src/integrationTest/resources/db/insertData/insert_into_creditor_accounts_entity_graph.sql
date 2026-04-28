/**
* OPAL Program
*
* MODULE      : insert_into_creditor_accounts_entity_graph.sql
*
* DESCRIPTION : Inserts Creditor Account graph data for repository integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 20/04/2026    Codex        1.0         Added creditor account entity graph integration test data
*
**/

INSERT INTO business_units
(
business_unit_id,business_unit_name,business_unit_code,business_unit_type,
account_number_prefix,parent_business_unit_id,opal_domain,welsh_language
)
VALUES
(951,'Creditor Graph Business Unit','CGBU','Area','CG',NULL,'Fines',false);

INSERT INTO major_creditors
(
major_creditor_id, business_unit_id, major_creditor_code,
name, address_line_1, address_line_2, address_line_3, postcode
)
VALUES
(950001, 951, 'CG01',
'Creditor Graph Major', '1 Graph Lane', 'Graph Town', 'Graph City', 'CG1 1CG');

INSERT INTO creditor_accounts
(
creditor_account_id, business_unit_id, account_number,
creditor_account_type, prosecution_service, major_creditor_id,
minor_creditor_party_id, from_suspense, hold_payout, pay_by_bacs,
bank_sort_code, bank_account_number, bank_account_name,
bank_account_reference, bank_account_type, version_number, last_changed_date
)
VALUES
(950010, 951, 'CG123456',
'MJ', false, 950001,
NULL, false, false, true,
'112233', '12345678', 'Graph Major',
'CREDGRAPH', '1', 1, '2026-04-20 10:00:00');
