/**
* CGI OPAL Program
*
* MODULE      : defendant_account_parties.sql
*
* DESCRIPTION : Creates the DEFENDANT_ACCOUNT_PARTIES table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the DEFENDANT_ACCOUNT_PARTIES table for the Fines model
*
**/
CREATE TABLE defendant_account_parties 
(
 defendant_account_party_id       bigint       not null
,defendant_account_id             bigint       not null
,party_id                         bigint       not null
,association_type                 varchar(30)  not null
,debtor                           boolean      not null
,CONSTRAINT defendant_account_parties_pk PRIMARY KEY 
 (
   defendant_account_party_id	
 ) 
);

ALTER TABLE defendant_account_parties
ADD CONSTRAINT dap_defendant_account_id_fk FOREIGN KEY
(
  defendant_account_id 
)
REFERENCES defendant_accounts
(
  defendant_account_id 
);

ALTER TABLE defendant_account_parties
ADD CONSTRAINT dap_party_id_fk FOREIGN KEY
(
  party_id 
)
REFERENCES parties
(
  party_id 
);

COMMENT ON COLUMN defendant_account_parties.defendant_account_party_id IS 'Unique ID of this record';
COMMENT ON COLUMN defendant_account_parties.defendant_account_id IS 'ID of the defendant account';
COMMENT ON COLUMN defendant_account_parties.party_id IS 'ID of the party associated ';
COMMENT ON COLUMN defendant_account_parties.association_type IS 'The party''s association type to the defendant account (Defendant or Parent/Guardian)';
COMMENT ON COLUMN defendant_account_parties.debtor IS 'If this party is responsible for paying the account';
