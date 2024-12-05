/**
* OPAL Program
*
* MODULE      : miscellaneous_accounts.sql
*
* DESCRIPTION : Create the table MISCELLANEOUS_ACCOUNTS in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 23/04/2024    A Dennis    1.0         PO-284 Create the table MISCELLANEOUS_ACCOUNTS in the Fines model
*
**/
CREATE TABLE miscellaneous_accounts 
(
 miscellaneous_account_id    bigint       not null
,business_unit_id            smallint     not null
,account_number              varchar(20)  not null
,party_id                    bigint       not null
,CONSTRAINT miscellaneous_accounts_pk PRIMARY KEY 
 (
  miscellaneous_account_id	
 )  
);

ALTER TABLE miscellaneous_accounts
ADD CONSTRAINT ma_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE miscellaneous_accounts
ADD CONSTRAINT ma_party_id_fk FOREIGN KEY
(
  party_id 
)
REFERENCES parties
(
  party_id 
);

COMMENT ON COLUMN miscellaneous_accounts.miscellaneous_account_id IS 'Unique ID of this record';
COMMENT ON COLUMN miscellaneous_accounts.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN miscellaneous_accounts.account_number IS 'Account number unique within the business unit';
COMMENT ON COLUMN miscellaneous_accounts.party_id IS 'The person or organisation this account belongs to';
