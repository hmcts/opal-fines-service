/**
* OPAL Program
*
* MODULE      : account_transfer_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table ACCOUNT_TRANSFERS. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 05/12/2023    A Dennis    1.0         PO-127 Creates the Sequence to be used to generate the Primary key for the table ACCOUNT_TRANSFERS
*
**/
CREATE SEQUENCE IF NOT EXISTS account_transfer_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY account_transfers.account_transfer_id;
