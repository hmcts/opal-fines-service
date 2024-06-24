/**
* OPAL Program
*
* MODULE      : create_print_jobs_table.sql
*
* DESCRIPTION : Creates the print_jobs table to store print jobs for asynchronous processing.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------
* 18/04/2024    Tom Reed 1.0        Initial creation of the print_jobs table
**/

CREATE TABLE print_job (
    print_job_id BIGINT NOT NULL,
    batch_uuid UUID NOT NULL,
    job_uuid UUID NOT NULL,
    xml_data TEXT NOT NULL,
    doc_type VARCHAR(50) NOT NULL,
    doc_version VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT print_job_id_pk PRIMARY KEY
     (
       print_job_id
     )
);


CREATE INDEX idx_status ON print_job(status);



