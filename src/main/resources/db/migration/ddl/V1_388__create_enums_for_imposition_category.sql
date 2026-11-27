/**
* OPAL Program
*
* MODULE      : create_enums_for_imposition_category.sql
*
* DESCRIPTION : Create ENUMs for Imposition Category and Context ENUM for function f_get_item_num_for_imp_cat
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
