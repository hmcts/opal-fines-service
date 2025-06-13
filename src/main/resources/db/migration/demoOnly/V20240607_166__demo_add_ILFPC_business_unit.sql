/**
* OPAL Program
*
* MODULE      : add_ILFPC_business_unit.sql
*
* DESCRIPTION : Add the business named ILFPC because it is referenced in the Courts reference data. It exists in LIVE but not in the Business Unit spreadsheet so was not laoded when Business Units reference data was loaded. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 07/06/2024    A Dennis    1.0         PO-308 Add the business named ILFPC because it is referenced in the Courts reference data. It exists in LIVE but not in the Business Unit spreadsheet so was not laoded when Business Units reference data was loaded. 
*
**/
INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type, account_number_prefix, parent_business_unit_id, opal_domain) VALUES (100, 'ILFPC', '71', 'Accounting Division', NULL, NULL, 'Fines');
