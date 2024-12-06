/**
* CGI OPAL Program
*
* MODULE      : update_offences.sql
*
* DESCRIPTION : Update offences table to use their parent business unit ids as a result of Business Units reference data loaded from Legacy GoB system test Oracle database.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 10/09/2024    A Dennis    1.0         PO-755 Update offences table to use their parent business unit ids as a result of Business Units reference data loaded from Legacy GoB system test Oracle database.
*                                       Note that the UPDATE statements have been arranged in order so that an UPDATE would not be overwritten by another UPDATE further down.
*
**/
UPDATE offences
SET   business_unit_id = 52
WHERE business_unit_id = 1;

UPDATE offences
SET   business_unit_id = 22
WHERE business_unit_id = 39;

UPDATE offences
SET   business_unit_id = 1020
WHERE business_unit_id = 40;

UPDATE offences
SET   business_unit_id = 1037
WHERE business_unit_id = 42;

UPDATE offences
SET   business_unit_id = 1042
WHERE business_unit_id = 12;

UPDATE offences
SET   business_unit_id = 45
WHERE business_unit_id = 35;
