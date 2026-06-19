/**
* OPAL Program
*
* MODULE      : alter_tills_add_new_columns.sql
*
* DESCRIPTION : Add source, status, totals, ownership, interface file, and creation details to TILLS.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 10/06/2026    C Cho       1.0         PO-2580 Add summary columns to TILLS.
*
**/

CREATE TYPE t_till_status_enum AS ENUM ('Created', 'Processing', 'Failed', 'Allocated');

ALTER TABLE tills
    ADD COLUMN source t_interface_file_source_enum,
    ADD COLUMN status t_till_status_enum,
    ADD COLUMN total_amount DECIMAL(18,2),
    ADD COLUMN interface_file_id BIGINT,
    ADD COLUMN payments_count SMALLINT,
    ADD COLUMN owned_by_name VARCHAR(100),
    ADD COLUMN auto_payment BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN created_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL;

COMMENT ON COLUMN tills.source IS 'The party that sent the associated interface file or manual payments in which will be passed on to Tills. Specific values can be found in the DB LLD on Confluence.';
COMMENT ON COLUMN tills.status IS 'Confirmation if the till has been created. Set to: Created when first created. Then Allocated once allocated by the functionality that allocates Tills.';
COMMENT ON COLUMN tills.total_amount IS 'As payments in are created for a till, the payment amounts are added up and the total is then used to update this total_amount column.';
COMMENT ON COLUMN tills.interface_file_id IS 'If it has a value then it enables one to tell the Interface File that was processed in order for the Till to created. Foreign key to INTERFACE_FILES tables.';
COMMENT ON COLUMN tills.payments_count IS 'As payments in are created for a till, the number of payments in records are added up and the total is then used to update this payments_count column. The backend will not have to compute this value and this column will also be used during Manual Payments In.';
COMMENT ON COLUMN tills.owned_by_name IS 'The name of the till owner.';
COMMENT ON COLUMN tills.auto_payment IS 'When the till by Auto Payments In then True, else false.';
COMMENT ON COLUMN tills.created_date IS 'The Till creation datetime.';

ALTER TABLE ONLY tills
    ADD CONSTRAINT till_interface_file_id_fk FOREIGN KEY (interface_file_id) REFERENCES interface_files(interface_file_id);
