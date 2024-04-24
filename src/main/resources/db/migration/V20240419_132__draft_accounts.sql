/**
* OPAL Program
*
* MODULE      : draft_accounts.sql
*
* DESCRIPTION : Create the table DRAFT_ACCOUNTS in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 19/04/2024    A Dennis    1.0         PO-284 Create the table DRAFT_ACCOUNTS in the Fines model
*
**/
CREATE TABLE draft_accounts 
(
 draft_account_id      bigint       not null
,business_unit_id      smallint     not null
,created_date          timestamp    not null
,created_by            varchar(20)  not null
,validated_date        timestamp
,validated_by          varchar(20)
,account               json         not null
,account_type          varchar(30)  not null
,account_id            bigint       
,CONSTRAINT draft_accounts_pk PRIMARY KEY 
 (
  draft_account_id	
 )  
);

ALTER TABLE draft_accounts
ADD CONSTRAINT dac_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

COMMENT ON COLUMN draft_accounts.draft_account_id IS 'Unique ID of this record';
COMMENT ON COLUMN draft_accounts.business_unit_id IS 'ID of the business unit this record belongs to';
COMMENT ON COLUMN draft_accounts.created_date IS 'Date this record was created';
COMMENT ON COLUMN draft_accounts.created_by IS 'ID of the user that created this record';
COMMENT ON COLUMN draft_accounts.validated_date IS 'Date the draft account was validated';
COMMENT ON COLUMN draft_accounts.validated_by IS 'ID of the user that validated the draft account';
COMMENT ON COLUMN draft_accounts.account IS 'The structured account data';
COMMENT ON COLUMN draft_accounts.account_type IS 'Type of account, such as Fixed Penalty Registration';
COMMENT ON COLUMN draft_accounts.account_id IS 'Account ID created on validation';
