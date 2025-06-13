/**
*
* OPAL Program
*
* MODULE      : bu_welsh_language.sql
*
* DESCRIPTION : Set Welsh Language option to be true for South Wales
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 23/07/2024    I Readman    1.0         PO-503 Update South Wales area so that column welsh_language is true
*
**/      
UPDATE business_units SET welsh_language = true 
 WHERE business_unit_name = 'South Wales' AND business_unit_type = 'Area';