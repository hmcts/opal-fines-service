/**
* CGI OPAL Program
*
* MODULE      : creditor_accounts.sql
*
* DESCRIPTION : Creates the CREDITOR ACCOUNTS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/04/2024    A Dennis    1.0         PO-200 Creates the CREDITOR_ACCCOUNTS table for the Fines model
*
**/
CREATE TABLE creditor_accounts 
(
 creditor_account_id         bigint       not null
,business_unit_id            smallint     not null
,account_number              varchar(20)  not null
,creditor_account_type       varchar(2)   not null
,prosecution_service         boolean      not null
,major_creditor_id           bigint
,minor_creditor_party_id     bigint
,from_suspense               boolean      not null
,hold_payout                 boolean      not null
,pay_by_bacs                 boolean      not null
,bank_sort_code              varchar(6)
,bank_account_number         varchar(10)
,bank_account_name           varchar(18)
,bank_account_reference      varchar(18)
,bank_account_type           varchar(1)
,last_changed_date           timestamp
,CONSTRAINT creditor_accounts_pk PRIMARY KEY 
 (
   creditor_account_id	
 ) 
);

ALTER TABLE creditor_accounts
ADD CONSTRAINT ca_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);

ALTER TABLE creditor_accounts
ADD CONSTRAINT ca_major_creditor_id_fk FOREIGN KEY
(
  major_creditor_id 
)
REFERENCES major_creditors
(
  major_creditor_id 
);

ALTER TABLE creditor_accounts
ADD CONSTRAINT ca_minor_creditor_party_id_fk FOREIGN KEY
(
  minor_creditor_party_id 
)
REFERENCES parties
(
  party_id 
);

COMMENT ON COLUMN creditor_accounts.creditor_account_id IS 'Unique ID of this record';
COMMENT ON COLUMN creditor_accounts.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN creditor_accounts.account_number IS 'Account number unique within the business unit';
COMMENT ON COLUMN creditor_accounts.creditor_account_type IS 'The type of creditor account. Values: MN (Minor Creditor), MJ (Major Creditor), CF (Central Fund).';
COMMENT ON COLUMN creditor_accounts.prosecution_service IS 'Indicates a major creditor is the crown prosecution service';
COMMENT ON COLUMN creditor_accounts.major_creditor_id IS 'The major creditor that this account belongs to';
COMMENT ON COLUMN creditor_accounts.minor_creditor_party_id IS 'The person or organisation this account belongs to';
COMMENT ON COLUMN creditor_accounts.from_suspense IS 'If the creditor was created from a suspense transaction. If so, there will be no relating impositions for this creditor account.';
COMMENT ON COLUMN creditor_accounts.hold_payout IS 'If set, prevents paying out monies received to this account';
COMMENT ON COLUMN creditor_accounts.pay_by_bacs IS 'If the creditor is paid by BACS as opposed to cheque';
COMMENT ON COLUMN creditor_accounts.bank_sort_code IS 'Bank sort code for payments out';
COMMENT ON COLUMN creditor_accounts.bank_account_number IS 'Bank account number for payments out';
COMMENT ON COLUMN creditor_accounts.bank_account_name IS 'Bank account name for payments out';
COMMENT ON COLUMN creditor_accounts.bank_account_reference IS 'Bank account reference for payments out';
COMMENT ON COLUMN creditor_accounts.bank_account_type IS 'Bank account type number (0-5) for payments out';
COMMENT ON COLUMN creditor_accounts.last_changed_date IS 'The date that the account or party was last changed in Account Maintenance.';
