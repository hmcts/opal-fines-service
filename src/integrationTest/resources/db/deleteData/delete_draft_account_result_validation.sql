DELETE FROM results_legacy WHERE result_id IN ('DVOPEN', 'DVLGEN', 'DVOPIM', 'DVLGIM');
DELETE FROM results WHERE result_id IN ('DVOPEN', 'DVLGEN', 'DVOPIM', 'DVLGIM');
