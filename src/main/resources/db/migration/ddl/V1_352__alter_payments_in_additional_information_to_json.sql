/**
* OPAL Program
*
* MODULE      : alter_payments_in_additional_information_to_json.sql
*
* DESCRIPTION : Alter payments_in.additional_information from VARCHAR(500) to JSON.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 15/09/2026    P Brumby    1.0         PO-3393 - Alter payments_in.additional_information from VARCHAR(500) to JSON.
*
**/

ALTER TABLE payments_in
    ALTER COLUMN additional_information TYPE json
        USING additional_information::json;