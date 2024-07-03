/**
* OPAL Program
*
* MODULE      : print_performance_monitor_id_seq.sql
*
* DESCRIPTION : Creates the Sequence to be used to generate the Primary key for the table PRINT_PERFORMANCE_MONITOR. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 05/03/2024    A Dennis    1.0         PO-208 Creates the Sequence to be used to generate the Primary key for the table PRINT_PERFORMANCE_MONITOR
*
**/
CREATE SEQUENCE IF NOT EXISTS print_performance_monitor_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY print_performance_monitor.print_performance_monitor_id;
