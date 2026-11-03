/**
* OPAL Program
*
* MODULE      : add_payments_in_associated_record_type.sql
*
* DESCRIPTION : Add the payments_in associated record type for till allocation.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 12/10/2026    C Cho          1.0         PO-3446 - Add payments_in associated record type
*
**/

ALTER TYPE public.t_associated_record_type_enum
    ADD VALUE 'payments_in';
