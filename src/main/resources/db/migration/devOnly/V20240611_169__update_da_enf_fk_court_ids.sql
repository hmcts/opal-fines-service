/**
* OPAL Program
*
* MODULE      : update_da_enf_fk_court_ids.sql
*
* DESCRIPTION : Update the courts foreign key ids in DEFENDANT_ACCOUNTS and ENFORCEMENTS tables to match the new court_ids in the COURTS table after reference data load. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------
* 11/06/2024    A Dennis    1.0         PO-308 Update the courts foreign key ids in DEFENDANT_ACCOUNTS and ENFORCEMENTS tables to match the new court_ids in the COURTS table after reference data load. 
*
**/

------- Update DEFENDANT_ACCOUNTS table -----------------
UPDATE defendant_accounts
SET imposing_court_id     = 3064
  , enforcing_court_id    = 3064
  , last_hearing_court_id = 3064
WHERE imposing_court_id   = 500000000;

UPDATE defendant_accounts
SET imposing_court_id     = 1855
  , enforcing_court_id    = 1855
  , last_hearing_court_id = 1855
WHERE imposing_court_id   = 500000001;

UPDATE defendant_accounts
SET imposing_court_id     = 21
  , enforcing_court_id    = 21
  , last_hearing_court_id = 21
WHERE imposing_court_id   = 500000002;

UPDATE defendant_accounts
SET imposing_court_id     = 4736
  , enforcing_court_id    = 4736
  , last_hearing_court_id = 4736
WHERE imposing_court_id   = 500000003;

UPDATE defendant_accounts
SET imposing_court_id     = 2398
  , enforcing_court_id    = 2398
  , last_hearing_court_id = 2398
WHERE imposing_court_id   = 500000004;

UPDATE defendant_accounts
SET imposing_court_id     = 4727
  , enforcing_court_id    = 4727
  , last_hearing_court_id = 4727
WHERE imposing_court_id   = 500000005;

UPDATE defendant_accounts
SET imposing_court_id     = 3322
  , enforcing_court_id    = 3322
  , last_hearing_court_id = 3322
WHERE imposing_court_id   = 500000006;

UPDATE defendant_accounts
SET imposing_court_id     = 4744
  , enforcing_court_id    = 4744
  , last_hearing_court_id = 4744
WHERE imposing_court_id   = 500000007;

UPDATE defendant_accounts
SET imposing_court_id     = 1866
  , enforcing_court_id    = 1866
  , last_hearing_court_id = 1866
WHERE imposing_court_id   = 500000008;

UPDATE defendant_accounts
SET imposing_court_id     = 6293
  , enforcing_court_id    = 6293
  , last_hearing_court_id = 6293
WHERE imposing_court_id   = 500000009;

---- Update ENFORCEMENTS table -------------
UPDATE enforcements
SET hearing_court_id     = 6293
WHERE hearing_court_id   = 500000000;

UPDATE enforcements
SET hearing_court_id     = 1855
WHERE hearing_court_id   = 500000001;

UPDATE enforcements
SET hearing_court_id     = 21
WHERE hearing_court_id   = 500000002;

UPDATE enforcements
SET hearing_court_id     = 4736
WHERE hearing_court_id   = 500000003;

UPDATE enforcements
SET hearing_court_id     = 2398
WHERE hearing_court_id   = 500000004;

UPDATE enforcements
SET hearing_court_id     = 4727
WHERE hearing_court_id   = 500000005;

UPDATE enforcements
SET hearing_court_id     = 3322
WHERE hearing_court_id   = 500000006;

UPDATE enforcements
SET hearing_court_id     = 4744
WHERE hearing_court_id   = 500000007;

UPDATE enforcements
SET hearing_court_id     = 1866
WHERE hearing_court_id   = 500000008;

UPDATE enforcements
SET hearing_court_id     = 6293
WHERE hearing_court_id   = 500000009;
