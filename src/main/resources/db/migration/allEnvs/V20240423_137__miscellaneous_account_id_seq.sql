/**
* OPAL Program
*
* MODULE      : miscellaneous_account_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table MISCELLANEOUS_ACCOUNTS. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 23/04/2024    A Dennis    1.0         PO-284 Creates the Sequence to be used to generate the Primary key for the table MISCELLANEOUS_ACCOUNTS
*
**/
CREATE SEQUENCE IF NOT EXISTS miscellaneous_account_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY miscellaneous_accounts.miscellaneous_account_id;
