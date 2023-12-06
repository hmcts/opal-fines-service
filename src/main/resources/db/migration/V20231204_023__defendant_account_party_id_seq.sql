/**
* OPAL Program
*
* MODULE      : defendant_account_party_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table DEFENDANT_ACCOUNT_PARTIES. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the Sequence to be used to generate the Primary key for the table DEFENDANT_ACCOUNT_PARTIES
*
**/
CREATE SEQUENCE IF NOT EXISTS defendant_account_party_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY defendant_account_parties.defendant_account_party_id;
