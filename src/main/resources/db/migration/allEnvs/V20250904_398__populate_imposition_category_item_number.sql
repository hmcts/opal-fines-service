/**
* CGI OPAL Program
*
* MODULE      : populate_imposition_category_item_number.sql
*
* DESCRIPTION : Populate the IMPOSITION_CATEGORY_ITEM_NUMBER table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------
* 27/08/2025    TMc         1.0         PO-2082 - Populate the IMPOSITION_CATEGORY_ITEM_NUMBER table
*
**/
DELETE FROM imposition_category_item_number;

INSERT INTO imposition_category_item_number(imposition_category, item_number)
VALUES ('Compensation', 214)
     , ('Costs', 213)
     , ('Court Charge', 805)
     , ('Crown Prosecution Costs', 301)
     , ('Fines', 208)
     , ('Legal Aid', 209)
     , ('Victim Surcharge', 405)
     , ('Witness Expenses & Central Fund', 212);