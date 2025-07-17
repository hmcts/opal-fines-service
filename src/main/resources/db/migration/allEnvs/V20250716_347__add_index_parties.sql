/**
* CGI OPAL Program
*
* MODULE      : add_index_aliases.sql
*
* DESCRIPTION : Add indexes to the PARTIES table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------
* 16/07/2025    TMc         1.0         PO-1562 Add indexes to the PARTIES table
*
**/
CREATE INDEX pa_surname_idx ON parties(surname);
CREATE INDEX pa_organisation_name_idx ON parties(organisation_name);
CREATE INDEX pa_surname_forenames_idx ON parties(surname, forenames);
CREATE INDEX pa_surname_birthdate_idx ON parties(surname, birth_date);
CREATE INDEX pa_national_insurance_num_idx ON parties(national_insurance_number);
CREATE INDEX pa_address_line_1_idx ON parties(address_line_1);
CREATE INDEX pa_postcode_idx ON parties(postcode);
CREATE INDEX pa_address_line_1_postcode_idx ON parties(address_line_1, postcode);