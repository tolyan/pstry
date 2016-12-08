CREATE USER demo IDENTIFIED BY pass;
GRANT CONNECT TO demo;
GRANT ALL PRIVILEGES TO demo;

-- all further actions should be performed under user demo!!!!

CREATE TABLE task (
  id         NUMBER PRIMARY KEY,
  value      VARCHAR2(50) NOT NULL,
  time       TIMESTAMP    NOT NULL,
  created_at TIMESTAMP    NOT NULL
);

CREATE SEQUENCE task_pk_seq CACHE 100;
CREATE OR REPLACE TRIGGER task_ins
BEFORE INSERT ON task
FOR EACH ROW
WHEN (new.id IS NULL)
  BEGIN
    SELECT task_pk_seq.nextval
    INTO :new.id
    FROM dual;
  END task_ins;

CREATE TABLE result (
  id      NUMBER PRIMARY KEY,
  task_id NUMBER        NOT NULL,
  value   VARCHAR2(250) NOT NULL,
  CONSTRAINT fk_result_to_task
  FOREIGN KEY (task_id)
  REFERENCES task (id)
);

CREATE SEQUENCE result_pk_seq CACHE 100;
CREATE OR REPLACE TRIGGER result_ins
BEFORE INSERT ON result
FOR EACH ROW
WHEN (new.id IS NULL)
  BEGIN
    SELECT result_pk_seq.nextval
    INTO :new.id
    FROM dual;
  END result_ins;

--================================

-- stored procedure which will be submitted to the background jobs
CREATE OR REPLACE PROCEDURE calculateResult(taskId IN NUMBER)
IS
  BEGIN
    DECLARE concatenatedValue VARCHAR2(250);
      currentValue VARCHAR2(250);
    BEGIN
      select value into currentValue from task WHERE id = taskId;
      SELECT listagg(value, '')
      WITHIN GROUP (
        ORDER BY created_at DESC)
      INTO concatenatedValue
      FROM (SELECT
              value,
              created_at
            FROM task
            where id <> taskId
            ORDER BY created_at DESC)
      WHERE rownum <= 3;
      INSERT INTO result (task_id, value) VALUES (taskId, currentValue || concatenatedValue);
    END;
  END;

-- stored procedure which creates and adds to the queue new background scheduled jobs
CREATE OR REPLACE PROCEDURE submitTask(taskId IN NUMBER)
IS
  BEGIN
    DECLARE startTime TIMESTAMP;
    BEGIN
      SELECT time
      INTO startTime
      FROM task
      WHERE id = taskId;

      DBMS_SCHEDULER.CREATE_PROGRAM
      (program_name        => 'backgroundJob' || taskId
      , program_type        => 'STORED_PROCEDURE'
      , program_action      => 'calculateResult'
      , enabled             => FALSE
      , number_of_arguments => 1
      );
      DBMS_SCHEDULER.DEFINE_PROGRAM_ARGUMENT
      (program_name      => 'backgroundJob' || taskId
      , argument_position => 1
      , argument_name     => 'taskId'
      , argument_type     => 'NUMBER'
      );
      DBMS_SCHEDULER.ENABLE('backgroundJob' || taskId);

      DBMS_SCHEDULER.CREATE_SCHEDULE
      (schedule_name   => 'demoJobSchedule' || taskId
      , start_date      => startTime
      , repeat_interval => NULL
      , end_date        => SYSTIMESTAMP + INTERVAL '1' MONTH
      );

      DBMS_SCHEDULER.CREATE_JOB
      (job_name      => 'concatenationJob' || taskId
      , program_name  => 'backgroundJob' || taskId
      , schedule_name => 'demoJobSchedule' || taskId
      , auto_drop => TRUE
      , enabled       => FALSE
      );
      DBMS_SCHEDULER.SET_JOB_ANYDATA_VALUE
      (job_name      => 'concatenationJob' || taskId
      , argument_name  => 'taskId'
      , argument_value => ANYDATA.CONVERTNUMBER(taskId)
      );
      DBMS_SCHEDULER.ENABLE('concatenationJob' || taskId);
    END;
  END;