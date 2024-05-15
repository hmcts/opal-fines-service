/**
* OPAL Program
*
* MODULE      : aliases.sql
*
* DESCRIPTION : Create the table ALIASES in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 19/04/2024    A Dennis    1.0         PO-284 Create the table ALIASES in the Fines model
*
**/
CREATE TABLE aliases 
(
 alias_id         bigint         not null
,party_id         bigint         not null
,surname          varchar(50)    not null
,forenames        varchar(50)     
,initials         varchar(10)
,sequence_number  integer        not null
,CONSTRAINT aliases_pk PRIMARY KEY 
 (
  alias_id	
 )  
);

ALTER TABLE aliases
ADD CONSTRAINT alias_party_id_fk FOREIGN KEY
(
  party_id 
)
REFERENCES parties
(
  party_id 
);

COMMENT ON COLUMN aliases.alias_id IS 'Unique ID of this record';
COMMENT ON COLUMN aliases.party_id IS 'ID of the party the alias belongs to';
COMMENT ON COLUMN aliases.surname IS 'Alias surname';
COMMENT ON COLUMN aliases.forenames IS 'Alias forenames';
COMMENT ON COLUMN aliases.initials IS 'Alias initials';
COMMENT ON COLUMN aliases.sequence_number IS 'Account/party level lias sequence';
