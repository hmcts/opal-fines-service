/**
* OPAL Program
*
* MODULE      : update_results_fo_fopr1.sql
*
* DESCRIPTION : Update imposition_allocation_priority to 5 in the RESULTS table for FO and FOPR1.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------
* 03/10/2024    A Dennis    1.0         PO-854 Update imposition_allocation_priority to 5 in the RESULTS table for FO and FOPR1.
*
**/

UPDATE results
SET    imposition_allocation_priority =  5
WHERE  result_id                      IN ('FO', 'FOPR1');
