/**
* CGI OPAL Program
*
* MODULE      : update_defendant_accounts.sql
*
* DESCRIPTION : Update defendant_accounts table to use their parent business unit ids and any other details as a result of Business Units reference data loaded from Legacy GoB system test Oracle database.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 10/09/2024    A Dennis    1.0         PO-755 Update defendant_accounts table to use their parent business unit ids and any other details as a result of Business Units reference data loaded from Legacy GoB system test Oracle database.
*
**/
UPDATE defendant_accounts
SET   business_unit_id      = 78
    , imposing_court_id     = 3610
    , enforcing_court_id    = 3610
    , last_hearing_court_id = 3610
WHERE business_unit_id      = 69;

UPDATE defendant_accounts
SET   business_unit_id = 26
WHERE business_unit_id = 60;

UPDATE defendant_accounts
SET   business_unit_id = 47
WHERE business_unit_id = 43;

UPDATE defendant_accounts
SET   business_unit_id = 65
WHERE business_unit_id = 70;

UPDATE defendant_accounts
SET   business_unit_id = 66
WHERE business_unit_id = 68;

UPDATE defendant_accounts
SET   business_unit_id = 67
WHERE business_unit_id = 73;

UPDATE defendant_accounts
SET   business_unit_id = 73
WHERE business_unit_id = 71;

UPDATE defendant_accounts
SET   business_unit_id = 77
WHERE business_unit_id = 67;

UPDATE defendant_accounts
SET   business_unit_id = 80
WHERE business_unit_id = 61;

UPDATE defendant_accounts
SET   business_unit_id = 106
WHERE business_unit_id = 74;

-- Change my test data to use courts that now exist after deleting some courts that pointed to RM business units or were not in Legacy GoB business nits
UPDATE defendant_accounts
SET   imposing_court_id     = 3695
    , enforcing_court_id    = 3695
    , last_hearing_court_id = 3695
WHERE defendant_account_id  = 500000003;

UPDATE defendant_accounts
SET   imposing_court_id     = 3350
    , enforcing_court_id    = 3350
    , last_hearing_court_id = 3350
WHERE defendant_account_id  = 500000005;

UPDATE defendant_accounts
SET   imposing_court_id     = 3363
    , enforcing_court_id    = 3363
    , last_hearing_court_id = 3363
WHERE defendant_account_id  = 500000007;
