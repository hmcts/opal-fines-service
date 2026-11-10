/**
* OPAL Program
*
* MODULE      : create_f_get_item_num_for_imp_cat_and_enums.sql
*
* DESCRIPTION : Create Function f_get_item_num_for_imp_cat, for Imposition Category to Item Number lookup, and its related Enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -----------------------------------------------------------------------
* 10/11/2026    T McCallion    1.0         PO-11116 - Create lookup function for Imposition Category - Item Number
*
**/

CREATE TYPE t_imposition_category_enum AS ENUM (
    'Compensation', 
    'Costs', 
    'Court Charge', 
    'Crown Prosecution Costs', 
    'Fines', 
    'Legal Aid', 
    'Victim Surcharge',
    'Witness Expenses & Central Fund'
);

CREATE TYPE t_imp_cat_item_num_context_enum AS ENUM (
    'MAC'
);


CREATE OR REPLACE FUNCTION f_get_item_num_for_imp_cat(
    pi_imposition_category t_imposition_category_enum,
    pi_context t_imp_cat_item_num_context_enum
)
RETURNS control_totals.item_number%TYPE
LANGUAGE plpgsql
IMMUTABLE
PARALLEL SAFE
AS $$
/**
* OPAL Program
*
* MODULE      : f_get_item_num_for_imp_cat
*
* DESCRIPTION : Function to lookup an Item Number for a given Imposition Category, depending on the context.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -----------------------------------------------------------------------
* 10/11/2026    T McCallion    1.0         PO-11116 - Create lookup function for Imposition Category - Item Number
*
**/
DECLARE
    v_item_number control_totals.item_number%TYPE;
BEGIN
    CASE pi_context

        WHEN 'MAC' THEN
            CASE pi_imposition_category
                WHEN 'Compensation'                     THEN v_item_number := 214;
                WHEN 'Costs'                            THEN v_item_number := 213;
                WHEN 'Court Charge'                     THEN v_item_number := 805;
                WHEN 'Crown Prosecution Costs'          THEN v_item_number := 301;
                WHEN 'Fines'                            THEN v_item_number := 208;
                WHEN 'Legal Aid'                        THEN v_item_number := 209;
                WHEN 'Victim Surcharge'                 THEN v_item_number := 405;
                WHEN 'Witness Expenses & Central Fund'  THEN v_item_number := 212;
                ELSE
                    --Do Nothing
            END CASE;
        ELSE
            --Do Nothing
    END CASE;

    RETURN v_item_number;
END;
$$