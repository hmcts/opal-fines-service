/**
* OPAL Program
*
* MODULE      : insert_enforcement_account_types_data.sql
*
* DESCRIPTION : Insert reference data into ENFORCEMENT_ACCOUNT_TYPES
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ------------------------------------------------------------------------------------------------------
* 16/06/2026    T McCallion    1.0         PO-2456 - Populate ENFORCEMENT_ACCOUNT_TYPES table with its reference data for Auto Enforcement Config
*
**/

INSERT INTO enforcement_account_types(enforcement_account_type_id, account_type, enforcement_account_type, account_type_path, version_number, minimum_balance)
VALUES (NEXTVAL('enforcement_account_type_id_seq'), 'COL', 'COLH', 'H', 1, NULL),
       (NEXTVAL('enforcement_account_type_id_seq'), 'COL', 'COLL', 'L', 1, NULL),
       (NEXTVAL('enforcement_account_type_id_seq'), 'A',   'AH',   'H', 1, NULL),
       (NEXTVAL('enforcement_account_type_id_seq'), 'A',   'AL',   'L', 1, NULL),
       (NEXTVAL('enforcement_account_type_id_seq'), 'CO',  'COH',  'H', 1, NULL),
       (NEXTVAL('enforcement_account_type_id_seq'), 'CO',  'COL',  'L', 1, NULL),
       (NEXTVAL('enforcement_account_type_id_seq'), 'Y',   'YH',   'H', 1, NULL),
       (NEXTVAL('enforcement_account_type_id_seq'), 'Y',   'YL',   'L', 1, NULL);