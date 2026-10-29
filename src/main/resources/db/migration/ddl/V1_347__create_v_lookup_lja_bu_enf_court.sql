/**
* OPAL Program
*
* MODULE      : create_v_lookup_lja_bu_enf_court.sql
*
* DESCRIPTION : Create view for mapping between business unit, LJA and Enforcing Court
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -------------------------------------------------------------
* 12/10/2026    T McCallion    1.0         PO-10395 - DB - Fines - Create v_lookup_lja_bu_enf_court view
*
**/

CREATE OR REPLACE VIEW v_lookup_lja_bu_enf_court
AS 
WITH ci_bu_lja AS (
    SELECT business_unit_id, item_value::SMALLINT AS local_justice_area_id
      FROM configuration_items ci
     WHERE item_name = 'BUSINESS_UNIT_LJA' 
),
ci_bu_enf_crt AS (
    SELECT business_unit_id, item_value::SMALLINT AS court_code
      FROM configuration_items ci
     WHERE item_name = 'BUSINESS_UNIT_ENFORCING_COURT' 
)
SELECT bu.business_unit_id, lja.local_justice_area_id, c.court_id AS enforcing_court_id
 FROM business_units bu 
 JOIN ci_bu_lja
   ON ci_bu_lja.business_unit_id = bu.business_unit_id  
 JOIN ci_bu_enf_crt
   ON ci_bu_enf_crt.business_unit_id = bu.business_unit_id 
 JOIN local_justice_areas lja 
   ON lja.local_justice_area_id = ci_bu_lja.local_justice_area_id
 JOIN courts c
   ON c.business_unit_id = ci_bu_enf_crt.business_unit_id
  AND c.court_code = ci_bu_enf_crt.court_code;
   
COMMENT ON VIEW v_lookup_lja_bu_enf_court IS 'Retrieve Business Unit, LJA and related Enforcing Court';