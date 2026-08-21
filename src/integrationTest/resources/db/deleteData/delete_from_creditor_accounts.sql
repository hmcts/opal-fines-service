/**
* OPAL Program
*
* MODULE      : delete_from_creditor_accounts.sql
*
* DESCRIPTION : Deletes rows of data in CREDITOR ACCOUNTS table for the Integration Tests
*
* VERSION HISTORY:
*
* Date         Author      Version    Nature of Change
* ----------   -------     --------   ----------------------------------------------------------------------------------
* 11/08/2025   J.SHEEN      1.0        Deletes rows of data into CREDITOR ACCOUNTS table for the Integration Tests
*
**/

DELETE FROM major_creditors WHERE major_creditor_id = 0001;

DELETE FROM business_units WHERE business_unit_id = 992;
