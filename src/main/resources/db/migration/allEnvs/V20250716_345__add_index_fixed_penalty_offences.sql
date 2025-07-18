/**
* CGI OPAL Program
*
* MODULE      : add_index_fixed_penalty_offences.sql
*
* DESCRIPTION : Add index to the FIXED_PENALTY_OFFENCES table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------
* 16/07/2025    TMc         1.0         PO-1560 Add index fpo_ticket_number_idx to the FIXED_PENALTY_OFFENCES table
*
**/
CREATE INDEX fpo_ticket_number_idx ON fixed_penalty_offences(ticket_number);