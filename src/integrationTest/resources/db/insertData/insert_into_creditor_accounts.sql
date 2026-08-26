/**
* OPAL Program
*
* MODULE      : insert_into_creditor_accounts.sql
*
* DESCRIPTION : Insert rows of data into CREDITOR ACCOUNTS table for the Integration Tests
*
* VERSION HISTORY:
*
* Date         Author      Version    Nature of Change
* ----------   -------     --------   ----------------------------------------------------------------------------------
* 22/05/2025   R DODD      1.0        PO-1047 Insert rows of data into CREDITOR ACCOUNTS table for the Integration Tests
* 05/08/2025   J SHEEN     1.1        PO-2972 Insert data for major creditors R1B disabled integration test
**/

INSERT INTO major_creditors (
  major_creditor_id, business_unit_id, major_creditor_code,
  name, address_line_1, address_line_2,
  address_line_3, postcode
)
VALUES
  (0001, 78, 'AAAA',
 'AAAA Credit Services', 'Credit Lane',
 'Creditville', 'Crediton', 'CR1 1CR');

INSERT INTO creditor_accounts (
  creditor_account_id, business_unit_id, account_number,
  creditor_account_type, prosecution_service, major_creditor_id,
  minor_creditor_party_id, repayment, hold_payout, pay_by_bacs
)
VALUES
  (100000000001,78,'00000001A', 'MJ',
  false, 1, NULL, false, false, false);
