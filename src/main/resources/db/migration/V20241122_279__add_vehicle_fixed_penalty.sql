/**
* OPAL Program
*
* MODULE      : add_vehicle_fixed_penalty.sql
*
* DESCRIPTION : Add new column vehicle_fixed_penalty to the FIXED_PENALTY_OFFENCES table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------
* 22/11/2024    A Dennis    1.0         PO-996 Add new column vehicle_fixed_penalty to the FIXED_PENALTY_OFFENCES table
*
**/

ALTER TABLE fixed_penalty_offences 
ADD COLUMN vehicle_fixed_penalty BOOLEAN;

COMMENT ON COLUMN fixed_penalty_offences.vehicle_fixed_penalty IS 'A flag to indicate the type of fixed penalty offence, either: Vehicle or non-vehicle. If set then the FP is a vehicle FP. If null, then the FP is a non-vehicle FP';
