/**
* OPAL Program
*
* MODULE      : alter_enforcement_account_type_id_seq.sql
*
* DESCRIPTION : Alter sequence enforcement_account_type_id_seq so it starts with 1
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ------------------------------------------------------------------------------------------------------
* 16/06/2026    T McCallion    1.0         PO-2456 - Populate ENFORCEMENT_ACCOUNT_TYPES table with its reference data for Auto Enforcement Config
*
**/

ALTER SEQUENCE enforcement_account_type_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1
    RESTART;
