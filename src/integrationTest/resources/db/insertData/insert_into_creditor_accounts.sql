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

INSERT INTO business_units
(
business_unit_id,business_unit_name,business_unit_code,business_unit_type,
account_number_prefix,parent_business_unit_id,opal_domain,welsh_language
)
VALUES
(
992,'Business Unit 992','BUXY',
'Area', 'XX',NULL,
'Fines',false
);

INSERT INTO major_creditors
(
major_creditor_id, business_unit_id, major_creditor_code
, name, address_line_1, address_line_2
, address_line_3, postcode
)
VALUES
(
0001, 992, 'AAAA'
, 'AAAA Credit Services', 'Credit Lane', 'Creditville'
, 'Crediton', 'CR1 1CR'
);
