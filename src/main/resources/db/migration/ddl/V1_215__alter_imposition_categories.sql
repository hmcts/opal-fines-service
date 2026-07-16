/**
* OPAL Program
*
* MODULE      : alter_imposition_categories.sql
*
* DESCRIPTION : Alter imposition category reference table
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc            1.0         PO-3842 - Amend IMPOSITION_CATEGORY_ITEM_NUMBER table.
*                                          Rename table to IMPOSITION_CATEGORIES and add coded primary key
*
**/

ALTER TABLE imposition_category_item_number
    DROP CONSTRAINT IF EXISTS imposition_category_item_number_pk;

ALTER TABLE imposition_category_item_number
    RENAME TO imposition_categories;

ALTER TABLE imposition_categories
    ADD COLUMN imposition_category_id varchar(10);

COMMENT ON COLUMN imposition_categories.imposition_category_id IS 'Unique coded value for imposition_category.';

UPDATE imposition_categories
    SET imposition_category_id = CASE imposition_category
                                    WHEN 'Compensation'            THEN 'COMP'
                                    WHEN 'Costs'                   THEN 'COST'
                                    WHEN 'Court Charge'            THEN 'CC'
                                    WHEN 'Crown Prosecution Costs' THEN 'CPC'
                                    WHEN 'Fines'                   THEN 'FINE'
                                    WHEN 'Legal Aid'               THEN 'LA'
                                    WHEN 'Victim Surcharge'        THEN 'VS'
                                    WHEN 'Witness Expenses & Central Fund' THEN 'WECF'
                                 END;

ALTER TABLE imposition_categories
    ALTER COLUMN imposition_category_id SET NOT NULL;

ALTER TABLE imposition_categories
    ADD CONSTRAINT imposition_category_pk PRIMARY KEY (imposition_category_id);

ALTER TABLE imposition_categories
    ADD CONSTRAINT ic_imposition_category_uk UNIQUE (imposition_category);
