/**
* OPAL Program
*
* MODULE      : draft_account_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table DRAFT_ACCOUNTS. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 19/04/2024    A Dennis    1.0         PO-284 Creates the Sequence to be used to generate the Primary key for the table DRAFT_ACCOUNTS
*
**/
CREATE SEQUENCE IF NOT EXISTS draft_account_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY draft_accounts.draft_account_id;
