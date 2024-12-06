/**
* CGI OPAL Program
*
* MODULE      : add_aad_columns.sql
*
* DESCRIPTION : Add posted_by_aad column to PAYMENT_TERMS, ENFORCEMENTS and DEFENDANT_TRANSACTIONS tables for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------------
* 19/02/2024    A Dennis    1.0         PO-191 Add posted_by_aad column to PAYMENT_TERMS, ENFORCEMENTS and DEFENDANT_TRANSACTIONS tables for the Fines model
*
**/

ALTER TABLE payment_terms
ADD COLUMN posted_by_aad varchar(100);

ALTER TABLE payment_terms
ADD CONSTRAINT pt_posted_by_aad_fk FOREIGN KEY
(
  posted_by_aad 
)
REFERENCES users
(
  user_id 
);

ALTER TABLE enforcements
ADD COLUMN posted_by_aad varchar(100);

ALTER TABLE enforcements
ADD CONSTRAINT enf_posted_by_aad_fk FOREIGN KEY
(
  posted_by_aad 
)
REFERENCES users
(
  user_id 
);

ALTER TABLE defendant_transactions
ADD COLUMN posted_by_aad varchar(100);

ALTER TABLE defendant_transactions
ADD CONSTRAINT dt_posted_by_aad_fk FOREIGN KEY
(
  posted_by_aad 
)
REFERENCES users
(
  user_id 
);
