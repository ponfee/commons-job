TRUNCATE TABLE QRTZ_FIRED_TRIGGERS;
TRUNCATE TABLE QRTZ_PAUSED_TRIGGER_GRPS;
TRUNCATE TABLE QRTZ_SCHEDULER_STATE;
TRUNCATE TABLE QRTZ_LOCKS;
TRUNCATE TABLE QRTZ_SIMPLE_TRIGGERS;
TRUNCATE TABLE QRTZ_SIMPROP_TRIGGERS;
TRUNCATE TABLE QRTZ_CRON_TRIGGERS;
TRUNCATE TABLE QRTZ_BLOB_TRIGGERS;
TRUNCATE TABLE QRTZ_CALENDARS;
DELETE FROM QRTZ_TRIGGERS;
DELETE FROM QRTZ_JOB_DETAILS;

TRUNCATE TABLE t_sched_job;
TRUNCATE TABLE t_sched_log;
