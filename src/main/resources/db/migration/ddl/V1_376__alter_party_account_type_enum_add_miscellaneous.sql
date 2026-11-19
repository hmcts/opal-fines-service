/**
* OPAL Program
*
* MODULE      : alter_party_account_type_enum_add_miscellaneous.sql
*
* DESCRIPTION : Add Miscellaneous party account type
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 19/11/2026    C Cho          1.0         PO-11487 - Add Miscellaneous party account type
*
**/

ALTER TYPE t_party_account_type_enum
    ADD VALUE IF NOT EXISTS 'Miscellaneous';