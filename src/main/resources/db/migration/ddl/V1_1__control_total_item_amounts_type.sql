/**
* OPAL Program
*
* MODULE      : control_total_item_amount_type.sql
*
* DESCRIPTION : Add database composite data type to be used as array of record in procedure p_adjust_control_totals input parameter.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 13/11/2026    S Akinkugbe          1.0          PO-3461  - Add Adjust Control Totals Common procedure dependency 
*
**/
CREATE TYPE control_total_item_amount_type AS (
	control_total_type varchar(10),
	item_number smallint,
	amount numeric(18,2));

COMMENT ON TYPE control_total_item_amount_type IS 'Type to be used as array of record in procedure p_adjust_control_totals input parameter.';
