/**
* OPAL Program
*
* MODULE      : update_dependent_business_units.sql
*
* DESCRIPTION : The existing test data values for business_unit_id in dependent OPAL database tables updated to business unit id values that can be found in the Legacy GoB Oracle database
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 19/03/2024    A Dennis    1.0         PO-244 The existing test data values for business_unit_id in dependent OPAL database tables updated to business unit id values that can be found in the Legacy GoB Oracle database.
*
**/

-- ========= DEFENDANT_ACCOUNTS table ========

UPDATE defendant_accounts
SET    business_unit_id  = 69
WHERE  business_unit_id  = 5000;

UPDATE defendant_accounts
SET    business_unit_id  = 94
WHERE  business_unit_id  = 5001;

UPDATE defendant_accounts
SET    business_unit_id  = 95
WHERE  business_unit_id  = 5002;

UPDATE defendant_accounts
SET    business_unit_id  = 96
WHERE  business_unit_id  = 5003;

UPDATE defendant_accounts
SET    business_unit_id  = 13
WHERE  business_unit_id  = 5004;

UPDATE defendant_accounts
SET    business_unit_id  = 16
WHERE  business_unit_id  = 5005;

UPDATE defendant_accounts
SET    business_unit_id  = 17
WHERE  business_unit_id  = 5006;

UPDATE defendant_accounts
SET    business_unit_id  = 19
WHERE  business_unit_id  = 5007;

UPDATE defendant_accounts
SET    business_unit_id  = 32
WHERE  business_unit_id  = 5008;

UPDATE defendant_accounts
SET    business_unit_id  = 64
WHERE  business_unit_id  = 5009;

-- ========= COURTS table ========

UPDATE courts
SET    business_unit_id  = 69
WHERE  business_unit_id  = 5000;

UPDATE courts
SET    business_unit_id  = 94
WHERE  business_unit_id  = 5001;

UPDATE courts
SET    business_unit_id  = 95
WHERE  business_unit_id  = 5002;

UPDATE courts
SET    business_unit_id  = 96
WHERE  business_unit_id  = 5003;

UPDATE courts
SET    business_unit_id  = 13
WHERE  business_unit_id  = 5004;

UPDATE courts
SET    business_unit_id  = 16
WHERE  business_unit_id  = 5005;

UPDATE courts
SET    business_unit_id  = 17
WHERE  business_unit_id  = 5006;

UPDATE courts
SET    business_unit_id  = 19
WHERE  business_unit_id  = 5007;

UPDATE courts
SET    business_unit_id  = 32
WHERE  business_unit_id  = 5008;

UPDATE courts
SET    business_unit_id  = 64
WHERE  business_unit_id  = 5009;

-- ========= PRISONS table ========

UPDATE prisons
SET    business_unit_id  = 69
WHERE  business_unit_id  = 5000;

UPDATE prisons
SET    business_unit_id  = 94
WHERE  business_unit_id  = 5001;

UPDATE prisons
SET    business_unit_id  = 95
WHERE  business_unit_id  = 5009;

UPDATE prisons
SET    business_unit_id  = 96
WHERE  business_unit_id  = 5003;

UPDATE prisons
SET    business_unit_id  = 13
WHERE  business_unit_id  = 5004;

UPDATE prisons
SET    business_unit_id  = 17
WHERE  business_unit_id  = 5008;

UPDATE prisons
SET    business_unit_id  = 19
WHERE  business_unit_id  = 5007;

-- ========= ENFORCERS table ========

UPDATE enforcers
SET    business_unit_id  = 69
WHERE  business_unit_id  = 5000;

UPDATE enforcers
SET    business_unit_id  = 94
WHERE  business_unit_id  = 5001;

UPDATE enforcers
SET    business_unit_id  = 95
WHERE  business_unit_id  = 5002;

UPDATE enforcers
SET    business_unit_id  = 96
WHERE  business_unit_id  = 5003;

UPDATE enforcers
SET    business_unit_id  = 13
WHERE  business_unit_id  = 5004;

UPDATE enforcers
SET    business_unit_id  = 16
WHERE  business_unit_id  = 5005;

UPDATE enforcers
SET    business_unit_id  = 17
WHERE  business_unit_id  = 5006;

UPDATE enforcers
SET    business_unit_id  = 19
WHERE  business_unit_id  = 5007;

UPDATE enforcers
SET    business_unit_id  = 32
WHERE  business_unit_id  = 5008;

UPDATE enforcers
SET    business_unit_id  = 64
WHERE  business_unit_id  = 5009;

-- ========= TILLS table ========

UPDATE tills
SET    business_unit_id  = 69
WHERE  business_unit_id  = 5000;

UPDATE tills
SET    business_unit_id  = 94
WHERE  business_unit_id  = 5001;

UPDATE tills
SET    business_unit_id  = 95
WHERE  business_unit_id  = 5002;

UPDATE tills
SET    business_unit_id  = 96
WHERE  business_unit_id  = 5003;

UPDATE tills
SET    business_unit_id  = 13
WHERE  business_unit_id  = 5004;

UPDATE tills
SET    business_unit_id  = 16
WHERE  business_unit_id  = 5005;

UPDATE tills
SET    business_unit_id  = 17
WHERE  business_unit_id  = 5006;

UPDATE tills
SET    business_unit_id  = 19
WHERE  business_unit_id  = 5007;

UPDATE tills
SET    business_unit_id  = 32
WHERE  business_unit_id  = 5008;

UPDATE tills
SET    business_unit_id  = 64
WHERE  business_unit_id  = 5009;

-- ========= MIS-DEBTORS table ========

UPDATE mis_debtors
SET    business_unit_id  = 69
WHERE  business_unit_id  = 5000;

UPDATE mis_debtors
SET    business_unit_id  = 94
WHERE  business_unit_id  = 5001;

UPDATE mis_debtors
SET    business_unit_id  = 95
WHERE  business_unit_id  = 5002;

UPDATE mis_debtors
SET    business_unit_id  = 96
WHERE  business_unit_id  = 5003;

UPDATE mis_debtors
SET    business_unit_id  = 13
WHERE  business_unit_id  = 5004;

UPDATE mis_debtors
SET    business_unit_id  = 16
WHERE  business_unit_id  = 5005;

UPDATE mis_debtors
SET    business_unit_id  = 17
WHERE  business_unit_id  = 5006;
