/**
* CGI OPAL Program
*
* MODULE      : parties.sql
*
* DESCRIPTION : Creates the PARTIES table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the PARTIES table for the Fines model
*
**/
CREATE TABLE parties 
(
 party_id                    bigint       not null
,organisation                boolean
,organisation_name           varchar(80)
,surname                     varchar(50)
,forenames                   varchar(50)
,initials                    varchar(2)
,title                       varchar(20)
,address_line_1              varchar(35)
,address_line_2              varchar(35)
,address_line_3              varchar(35)
,address_line_4              varchar(35)
,address_line_5              varchar(35)
,postcode                    varchar(10)
,account_type                varchar(20)
,birth_date                  timestamp
,age                         smallint
,national_insurance_number   varchar(10)
,last_changed_date           timestamp
,CONSTRAINT parties_pk PRIMARY KEY 
 (
   party_id	
 ) 
);

COMMENT ON COLUMN parties.party_id IS 'Unique ID of this record';
COMMENT ON COLUMN parties.organisation IS 'Indicates if this party is an organisation or person';
COMMENT ON COLUMN parties.organisation_name IS 'Organisation name. Null for persons.';
COMMENT ON COLUMN parties.surname IS 'Person surname. Null for organisations. This will be the full name for parent/guardians but can be modified once GoB is decommissioned';
COMMENT ON COLUMN parties.forenames IS 'Person forenames. Null for organisations.';
COMMENT ON COLUMN parties.initials IS 'Person initials. Null for organisations.';
COMMENT ON COLUMN parties.title IS 'Person title. Null for organisations.';
COMMENT ON COLUMN parties.address_line_1 IS 'Address line 1';
COMMENT ON COLUMN parties.address_line_2 IS 'Address line 2';
COMMENT ON COLUMN parties.address_line_3 IS 'Address line 3';
COMMENT ON COLUMN parties.address_line_4 IS 'Address line 4. New field to handle larger addresses to be used once GoB has been decommissioned';
COMMENT ON COLUMN parties.address_line_5 IS 'Address line 5. New field to handle larger addresses to be used once GoB has been decommissioned';
COMMENT ON COLUMN parties.postcode IS 'Postcode';
COMMENT ON COLUMN parties.account_type IS 'The account type that the party is associated. We shouldn''t merge parties of different account types. We don''t want someone to amend a creditor and it affect defendant accounts if they are also a debtor. A party should not exist if no accounts exist.';
COMMENT ON COLUMN parties.birth_date IS 'Person date of birth (only applies to an account party)';
COMMENT ON COLUMN parties.age IS 'Person estimated if birth date not known (only applies to an account party)';
COMMENT ON COLUMN parties.national_insurance_number IS 'Person national insurance number (only applies to an account party)';
COMMENT ON COLUMN parties.last_changed_date IS 'Date this party was last changed in Account Maintenance.';
