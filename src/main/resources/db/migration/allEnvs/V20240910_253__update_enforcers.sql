/**
* CGI OPAL Program
*
* MODULE      : update_enforcers.sql
*
* DESCRIPTION : Update enforcers table to use their parent business unit ids as a result of Business Units reference data loaded from Legacy GoB system test Oracle database.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 10/09/2024    A Dennis    1.0         PO-755 Update enforcers table to use their parent business unit ids as a result of Business Units reference data loaded from Legacy GoB system test Oracle database
*
**/

UPDATE enforcers
SET   business_unit_id = 78
WHERE business_unit_id = 69;

UPDATE enforcers
SET   business_unit_id = 128
WHERE business_unit_id = 94;

UPDATE enforcers
SET   business_unit_id = 129
WHERE business_unit_id = 95;

UPDATE enforcers
SET   business_unit_id = 130
WHERE business_unit_id = 96;

UPDATE enforcers
SET   business_unit_id = 14
WHERE business_unit_id = 13;

UPDATE enforcers
SET   business_unit_id = 92
WHERE business_unit_id = 16;

UPDATE enforcers
SET   business_unit_id = 26
WHERE business_unit_id = 17;

UPDATE enforcers
SET   business_unit_id = 99
WHERE business_unit_id = 19;

UPDATE enforcers
SET   business_unit_id = 28
WHERE business_unit_id = 32;

UPDATE enforcers
SET   business_unit_id = 103
WHERE business_unit_id = 64;
