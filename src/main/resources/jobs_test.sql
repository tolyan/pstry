CREATE USER demo IDENTIFIED BY pass;
GRANT CONNECT TO demo;
GRANT ALL PRIVILEGES TO demo;

-- all further actions should be performed under user demo!!!!

CREATE TABLE task (
  id    NUMBER PRIMARY KEY,
  value VARCHAR2(50) NOT NULL,
  time  TIMESTAMP    NOT NULL
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
  concatenatedValue VARCHAR2(250);
  BEGIN
    SELECT listagg(task.value, ' ')
    WITHIN GROUP (
      ORDER BY task.id)
    INTO concatenatedValue
    FROM task
    WHERE ROWNUM <= 3;
    INSERT INTO result (task_id, value) VALUES (task_id, concatenatedValue);
  END;

-- sample stored procedure to check scheduled jobs
CREATE OR REPLACE PROCEDURE procPrintHelloWorld
IS
  BEGIN

    insert into task(value, time) VALUES ('value2', current_date);

  END;

-- create a sample job. We don't need repetition, we need to fire it only once
begin
  dbms_scheduler.create_job(
      job_name => 'Scheduled_job_for_demo',
      job_type => 'STORED_PROCEDURE',
      job_action => 'procPrintHelloWorld',
      start_date => sysdate + (5/(24*60)),
      repeat_interval => 'FREQ=MINUTELY; interval=1',
      enabled => TRUE,
      comments => 'Runtime: Every day every minute');
end;
