/**
* OPAL Program
*
* MODULE      : update_allocations_dev_test_data_for_r1b_enums.sql
*
* DESCRIPTION : Update allocations dev test data for R1b enum values
*
* VERSION HISTORY:
*
* Date          Author    Version     Nature of Change
* ----------    ------    --------    ----------------------------------------------------------------------------
* 03/06/2026    TMc       1.0         PO-3618 - Update columns on ALLOCATIONS table to use PostgreSQL ENUM
*
**/

UPDATE allocations
SET transaction_type = 'PAYMNT'
WHERE transaction_type = 'PAYMENT';
