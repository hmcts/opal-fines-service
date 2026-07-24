/**
* OPAL Program
*
* MODULE      : create_r1b_enums.sql
*
* DESCRIPTION : Create new ENUM data types for R1B
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -----------------------------------------------------------------------------------
* 02/06/2026    T McCallion    1.0         PO-3609 - Create PostgreSQL R1b Enumerated data types to replace VARCHAR data types
*
**/
CREATE TYPE t_allocation_transaction_type_enum AS ENUM ('DISHCQ', 'FR-SUS', 'MADJ', 'PAYMNT', 'REVPAY', 'RVWOFF', 'TFO', 'TFO IN', 'WRTOFF');
CREATE TYPE t_bacs_status_enum AS ENUM ('T', 'R', 'C', 'S', 'F', 'X', 'U');
CREATE TYPE t_cheque_allocation_type_enum AS ENUM ('COMP', 'REPAYW');
CREATE TYPE t_cheque_status_enum AS ENUM ('D', 'N', 'P', 'Q', 'W', 'X');
CREATE TYPE t_frequency_period_enum AS ENUM ('D', 'F', 'M', 'W');
CREATE TYPE t_interface_file_source_enum AS ENUM ('NATWEST', 'ALLPAY', 'ALLPAY_DD', 'BARCLAYCARD', 'BTECKOH', 'DWP', 'CDER', 'JACOBS', 'MARSTON', 'OTHER');
CREATE TYPE t_interface_job_status_enum AS ENUM ('CREATED', 'PROCESSED', 'IGNORED', 'FAILED', 'COMPLETED');
CREATE TYPE t_pi_destination_type_enum AS ENUM ('F', 'S', 'C');
CREATE TYPE t_print_job_status_enum AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED');
CREATE TYPE t_imposition_creditor_enum AS ENUM ('Any', 'CF', '!CPS', 'CPS');
CREATE TYPE t_result_type_enum AS ENUM ('Action', 'Result');
CREATE TYPE t_suspense_item_type_enum AS ENUM ('CC', 'CF', 'FA', 'FB', 'IN', 'LA', 'MA', 'MB', 'MC', 'MS', 'OM', 'OC', 'OF', 'UN');
CREATE TYPE t_reversed_enum AS ENUM ('R', 'D');
CREATE TYPE t_suspense_transaction_type_enum AS ENUM ('CC', 'CF', 'DQ', 'FA', 'FB', 'IN', 'LA', 'MA', 'MB', 'MC', 'MS', 'OM', 'OC', 'OF', 'RP', 'RV', 'UN', 'XF', 'XP');