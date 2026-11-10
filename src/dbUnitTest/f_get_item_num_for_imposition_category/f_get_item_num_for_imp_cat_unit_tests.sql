\timing

/**
* OPAL Program
*
* MODULE      : f_get_item_num_for_imp_cat_unit_tests.sql
*
* DESCRIPTION : Unit tests for lookup function f_get_item_num_for_imp_cat
*               It should return an Item Number value for the given Imposition Category and Context
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 10/11/2026    T McCallion    1.0         PO-11116 - Create lookup function for Imposition Category - Item Number
*
**/

DO LANGUAGE 'plpgsql' $$
DECLARE
    v_context_mac   t_imp_cat_item_num_context_enum := 'MAC';

    v_item_number   control_totals.item_number%TYPE;
BEGIN
    RAISE NOTICE '=== TEST 1 - Context = MAC ===';

    ASSERT (SELECT f_get_item_num_for_imp_cat('Compensation', v_context_mac)) = 214,
           'Item Number, context MAC, for Compensation should be 214';

    ASSERT (SELECT f_get_item_num_for_imp_cat('Costs', v_context_mac)) = 213,
           'Item Number, context MAC, for Compensation should be 213';

    ASSERT (SELECT f_get_item_num_for_imp_cat('Court Charge', v_context_mac)) = 805,
           'Item Number, context MAC, for Compensation should be 805';

    ASSERT (SELECT f_get_item_num_for_imp_cat('Crown Prosecution Costs', v_context_mac)) = 301,
           'Item Number, context MAC, for Compensation should be 301';

    ASSERT (SELECT f_get_item_num_for_imp_cat('Fines', v_context_mac)) = 208,
           'Item Number, context MAC, for Compensation should be 208';

    ASSERT (SELECT f_get_item_num_for_imp_cat('Legal Aid', v_context_mac)) = 209,
           'Item Number, context MAC, for Compensation should be 209';

    ASSERT (SELECT f_get_item_num_for_imp_cat('Victim Surcharge', v_context_mac)) = 405,
           'Item Number, context MAC, for Compensation should be 405';

    ASSERT (SELECT f_get_item_num_for_imp_cat('Witness Expenses & Central Fund', v_context_mac)) = 212,
           'Item Number, context MAC, for Compensation should be 212';

    RAISE NOTICE 'TEST 1 PASSED: For all Imposition Categories for context MAC';

END $$;

DO LANGUAGE 'plpgsql' $$
DECLARE
    v_context_mac   t_imp_cat_item_num_context_enum := 'MAC';

    v_item_number   control_totals.item_number%TYPE;
BEGIN
    --This scenario would only happen if an additional value was added to the Enum but the Function was not updated to include it.
    RAISE NOTICE '=== TEST 2 - Imposition Category not implemented in the Function ===';

    ALTER TYPE t_imposition_category_enum
        ADD VALUE IF NOT EXISTS 'Test_Only';
    COMMIT;

    ASSERT (SELECT f_get_item_num_for_imp_cat('Test_Only', v_context_mac)) IS NULL,
           'Item Number, context MAC, for an Imposition Category not implemented in the Function should return NULL';


    RAISE NOTICE 'TEST 2 PASSED: Imposition Category not implemented in the Function';

END $$;