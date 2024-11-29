/**
* OPAL Program
*
* MODULE      : cleanup_courts.sql
*
* DESCRIPTION : Delete the initial COURTS data, so we can later populate with data based on the courts reference data in Excel spreadsheet
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------------------------------------------------
* 07/06/2024    A Dennis    1.0         PO-308 Delete the initial COURTS data, so we can later populate with data based on the courts reference data in Excel spreadsheet
*/

-- DROP foreign keys to COURTS primary key before deleting initial courts data
ALTER TABLE defendant_accounts
DROP constraint IF EXISTS da_imposing_court_id_fk;

ALTER TABLE defendant_accounts
DROP constraint IF EXISTS da_enforcing_court_id_fk;

ALTER TABLE defendant_accounts
DROP constraint IF EXISTS da_last_hearing_court_id_fk;

ALTER TABLE enforcements
DROP constraint IF EXISTS enf_hearing_court_id_fk;

-- Delete from COURTS so we can later pouplate with data based on the COURTS reference data
DELETE FROM courts;
