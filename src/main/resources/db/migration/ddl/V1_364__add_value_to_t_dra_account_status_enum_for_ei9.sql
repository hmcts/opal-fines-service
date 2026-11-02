/**
* OPAL Program
*
* MODULE      : add_value_to_t_dra_account_status_enum_for_ei9.sql
*
* DESCRIPTION : Add value to t_dra_account_status_enum for EI9
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 20/10/2026    TT             1.0         PO-10684 - Add value to t_dra_account_status_enum for EI9.
*
**/

ALTER TYPE t_dra_account_status_enum ADD VALUE IF NOT EXISTS 'PUBLISHING_FAILURE_RESOLVED';
