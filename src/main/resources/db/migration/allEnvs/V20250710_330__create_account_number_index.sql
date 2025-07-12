/**
* CGI OPAL Program
*
* MODULE      : create_account_number_index.sql
*
* DESCRIPTION : Create the ACCOUNT_NUMBER_INDEX table, sequence and unique index in the OPAL Fines database.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------------------
* 08/07/2025    TMc         1.0         PO-899 Create the ACCOUNT_NUMBER_INDEX table, sequence and unique index in the OPAL Fines database.
*
**/
DROP TABLE IF EXISTS account_number_index;

CREATE TABLE account_number_index
(
    account_number_index_id BIGINT      NOT NULL
   ,business_unit_id        SMALLINT    NOT NULL 
   ,account_number          VARCHAR(20) NOT NULL
   ,account_index_type      VARCHAR(30)
   ,CONSTRAINT account_number_index_pk PRIMARY KEY (account_number_index_id)
   ,CONSTRAINT ani_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id)
);

COMMENT ON COLUMN account_number_index.account_number_index_id IS 'Unique ID of this record';
COMMENT ON COLUMN account_number_index.business_unit_id        IS 'ID of the relating business unit';
COMMENT ON COLUMN account_number_index.account_number          IS 'Account number unique within the business unit';
COMMENT ON COLUMN account_number_index.account_index_type      IS 'The target table the account is intended for (defendant_accounts, creditor_accounts or miscellaneous_accounts)';


CREATE UNIQUE INDEX ani_acc_num_bu_udx ON account_number_index (account_number, business_unit_id);


CREATE SEQUENCE account_number_index_seq
    INCREMENT 1 
    START 60000000000000 
    MINVALUE 60000000000000
    NO MAXVALUE 
    CACHE 1
    OWNED BY account_number_index.account_number_index_id;