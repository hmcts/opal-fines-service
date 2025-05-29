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
*
**/

INSERT INTO major_creditors
(
major_creditor_id, business_unit_id, major_creditor_code
, name, address_line_1, address_line_2
, address_line_3, postcode
)
VALUES
(
0001, 78, 'AAAA'
, 'AAAA Credit Services', 'Credit Lane', 'Creditville'
, 'Crediton', 'CR1 1CR'
);
