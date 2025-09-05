/**
* CGI OPAL Program
*
* MODULE      : create_imposition_category_item_number.sql
*
* DESCRIPTION : Create the IMPOSITION_CATEGORY_ITEM_NUMBER table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------
* 27/08/2025    TMc         1.0         PO-2082 - Create the IMPOSITION_CATEGORY_ITEM_NUMBER table
*
**/
DROP TABLE IF EXISTS imposition_category_item_number;

CREATE TABLE imposition_category_item_number
(
    imposition_category VARCHAR(40) NOT NULL
   ,item_number         SMALLINT    NOT NULL 
   ,CONSTRAINT imposition_category_item_number_pk PRIMARY KEY (imposition_category)
   --,CONSTRAINT icin_imposition_category_fk FOREIGN KEY (imposition_category) REFERENCES results(imposition_category)   ***** Can't do this - No unique constraint on RESULTS
);

COMMENT ON COLUMN imposition_category_item_number.imposition_category IS 'Financial category that monies for imposition are reported under. The imposition Category currently found in the results table.';
COMMENT ON COLUMN imposition_category_item_number.item_number         IS 'Control total item number.';