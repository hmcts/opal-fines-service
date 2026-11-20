/**
* OPAL Program
*
* MODULE      : create_view_v_lookup_lja_bu_enf_court_unit_tests.sql
*
* DESCRIPTION : Unit tests for v_lookup_lja_bu_enf_court view.
*               Tests verify mappings held in configuration_items.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------
* 12/10/2026    TMc         1.0         PO-10395 - Unit tests for Create v_lookup_lja_bu_enf_court view
*
**/
\timing

--Uses existing test data, loaded as part of PO-10397 and PO-10400
DO $$
DECLARE
    v_lookup_lja_bu_enf_court v_lookup_lja_bu_enf_court%ROWTYPE;
BEGIN
    RAISE NOTICE '=== TEST 1: Retrieve Business Unit and Enforcing Court for a LJA (1910) ===';

    SELECT *
      INTO STRICT v_lookup_lja_bu_enf_court
      FROM v_lookup_lja_bu_enf_court
     WHERE local_justice_area_id = 1910;

    ASSERT v_lookup_lja_bu_enf_court.business_unit_id = 26, 'business_unit_id should match';
    ASSERT v_lookup_lja_bu_enf_court.local_justice_area_id = 1910, 'local_justice_area_id should match';
    ASSERT v_lookup_lja_bu_enf_court.enforcing_court_id = 260000000013, 'enforcing_court_id should match';

    RAISE NOTICE 'TEST 1 PASSED';
END $$;
