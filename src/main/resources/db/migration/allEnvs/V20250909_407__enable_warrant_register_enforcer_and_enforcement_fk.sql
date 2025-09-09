/**
* OPAL Program
*
* MODULE      : enable_warrant_register_enforcer_and_enforcement_fk.sql
*
* DESCRIPTION : Update WARRANT_REGISTER table to add foreign key constraints for enforcer_id and enforcement_id column and add foreign key indexes.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 02/09/2025    P Brumby    1.0         PO-1770 Update WARRANT_REGISTER table to add foreign key constraints for enforcer_id referencing enforcers.enforcer_id and enforcement_id referencing enforcements.enforcement_id and for each foreign key add an index.
*
**/

ALTER TABLE warrant_register
ADD CONSTRAINT wr_enforcer_id_fk FOREIGN KEY
(
  enforcer_id
)
REFERENCES enforcers
(
  enforcer_id
);

CREATE INDEX wr_enforcer_id_idx ON warrant_register(enforcer_id);

ALTER TABLE warrant_register
ADD CONSTRAINT wr_enforcement_id_fk FOREIGN KEY
(
  enforcement_id
)
REFERENCES enforcements
(
  enforcement_id
);

CREATE INDEX wr_enforcement_id_idx ON warrant_register(enforcement_id);