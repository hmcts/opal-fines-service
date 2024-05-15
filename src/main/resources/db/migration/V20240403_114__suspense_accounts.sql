/**
* CGI OPAL Program
*
* MODULE      : suspense_accounts.sql
*
* DESCRIPTION : Creates the SUSPENSE_ACCOUNTS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/04/2024    A Dennis    1.0         PO-200 Creates the SUSPENSE_ACCOUNTS table for the Fines model
*
**/
CREATE TABLE suspense_accounts 
(
 suspense_account_id      bigint         not null
,business_unit_id         smallint       not null
,account_number           varchar(20)    not null
,CONSTRAINT suspense_accounts_pk PRIMARY KEY 
 (
  suspense_account_id	
 )  
);

ALTER TABLE suspense_accounts
ADD CONSTRAINT sa_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN suspense_accounts.suspense_account_id IS 'Unique ID of this record';
COMMENT ON COLUMN suspense_accounts.business_unit_id IS 'ID of the business unit this account belongs to';
COMMENT ON COLUMN suspense_accounts.account_number IS 'Account number';
