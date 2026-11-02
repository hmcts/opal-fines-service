/**
* OPAL Program
*
* MODULE      : populate_configuration_items_vpfpo_tactical_mapping.sql
*
* DESCRIPTION : Populate CONFIGURATION_ITEMS with VPFPO tactical mapping data - BU to Enf Court
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------
* 14/10/2026    TT         1.0         PO-10400 - source spreasheetL: CP_CCT-1339_Tactical_Mapping_v1_0.xlsx provided by CP.
*                                      Data in the source_enforcing CTE is derived from the source spreadsheet, where is_enf_cocurt = 'y'.
*                                      The six combinations in the excluded_mappings CTE are excluded because they are not found in PROD or NLE.
*                                      The script is rerunnable.
**/

WITH source_enforcing(business_unit_id, gob_court_code) AS (
    VALUES
        (5,10),
        (8,182),
        (9,101),
        (10,105),
        (11,202),
        (12,861),
        (14,5),
        (21,201),
        (22,20),
        (24,100),
        (26,113),
        (28,880),
        (29,901),
        (30,501),
        (31,322),
        (36,1),
        (38,803),
        (45,54),
        (47,2),
        (52,444),
        (57,111),
        (60,4),
        (61,620),
        (65,828),
        (66,855),
        (73,828),
        (77,828),
        (78,828),
        (82,104),
        (89,149),
        (92,301),
        (96,207),
        (97,902),
        (99,535),
        (103,126),
        (105,103),
        (106,70),
        (109,14),
        (110,7),
        (112,102),
        (116,300),
        (119,50),
        (124,704),
        (125,30),
        (126,130),
        (128,618),
        (129,604),
        (130,199),
        (135,8),
        (138,108),
        (139,220)
), excluded_mappings(business_unit_id, gob_court_code) AS (
    VALUES
        (45,54),
        (66,855),
        (97,902),
        (109,14),
        (110,7),
        (116,300)
)
INSERT INTO public.configuration_items
    (configuration_item_id, item_name, business_unit_id, item_value, item_values)
SELECT
    nextval('configuration_item_id_seq'),
    'BUSINESS_UNIT_ENFORCING_COURT',
    s.business_unit_id,
    s.gob_court_code::text,
    NULL
FROM source_enforcing s
LEFT JOIN excluded_mappings x
  ON x.business_unit_id = s.business_unit_id
 AND x.gob_court_code = s.gob_court_code
WHERE x.business_unit_id IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM public.configuration_items ci
      WHERE ci.item_name = 'BUSINESS_UNIT_ENFORCING_COURT'
        AND ci.business_unit_id = s.business_unit_id
        AND ci.item_value = s.gob_court_code::text
  );
