/**
* CGI OPAL Program
*
* MODULE      : print_performance_monitor.sql
*
* DESCRIPTION : Creates the PRINT_PERFORMANCE_MONITOR table for the Print service
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 28/02/2024    A Dennis    1.0         PO-208 Creates the PRINT_PERFORMANCE_MONITOR table for the Print service
*
**/
CREATE TABLE IF NOT EXISTS print_performance_monitor
(
 print_performance_monitor_id   bigint        not null
,uuid                           varchar(360)  not null
,document_type                  varchar(100)  not null
,date_rendered                  timestamp
,time_rendered                  bigint        not null
,render_size                    bigint        not null
,render_server                  varchar(60)   not null
,used_memory                    bigint        not null
,free_memory                    bigint        not null
,total_memory                   bigint        not null
,max_memory                     bigint        not null
,CONSTRAINT print_performance_monitor_id_pk  PRIMARY KEY
 (
   print_performance_monitor_id
 )
);

COMMENT ON COLUMN print_performance_monitor.print_performance_monitor_id IS 'Sequence generated primary key';
COMMENT ON COLUMN print_performance_monitor.uuid IS 'The UUID';
COMMENT ON COLUMN print_performance_monitor.document_type IS 'The Document Type';
COMMENT ON COLUMN print_performance_monitor.date_rendered IS 'Date PDF was rendered';
COMMENT ON COLUMN print_performance_monitor.time_rendered IS 'Time taken to render the PDF';
COMMENT ON COLUMN print_performance_monitor.render_size IS 'Size of the render';
COMMENT ON COLUMN print_performance_monitor.render_server IS 'The server used to render the request';
COMMENT ON COLUMN print_performance_monitor.used_memory IS 'The amount of memory used';
COMMENT ON COLUMN print_performance_monitor.free_memory IS 'Amount of free memory';
COMMENT ON COLUMN print_performance_monitor.total_memory IS 'Total memory used';
COMMENT ON COLUMN print_performance_monitor.max_memory IS 'The maximum amount of memory available';
