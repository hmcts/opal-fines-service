/**
* OPAL Program
*
* MODULE      : add_welsh_to_business_units.sql
*
* DESCRIPTION : Add welsh_languague column to the BUSINESS_UNITS table so to identify them in the database during data retrieval. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------
* 04/07/2024    A Dennis    1.0         PO-451 Add welsh_languague column to the BUSINESS_UNITS table so to identify them in the OPAL database. It does not exist in Legacy GoB.
*
**/
ALTER TABLE business_units
ADD COLUMN welsh_language        boolean;

COMMENT ON COLUMN business_units.welsh_language IS 'To identify if this is a welsh language business unit in Opal. It does not exist in Legacy GoB';
