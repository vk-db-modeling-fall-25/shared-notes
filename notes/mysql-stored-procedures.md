# MySQL: Stored Procedures

> A **stored procedure** is a named collection of procedural and SQL statements, stored in the database
- Why useful?
  - Better performance since stored and executed in the database rather than at the client
  - Facilitates code reuse

## Create

Example from the GTD model to create a stored procedure to get all overdue tasks:

```sql
DELIMITER //

CREATE PROCEDURE GetOverdueTasks()
BEGIN
    SELECT task_name, task_due
    FROM task 
    WHERE task_due IS NOT NULL AND task_due < CURDATE();
END //

DELIMITER ;
```

How it works:
1. Change the default delimiter to something other than the SQL delimiter ';'
1. Use the ```CREATE PROCEDURE``` statement
     - We can now use the ';' delimited in the procedure
2. Change the delimiter back to the default

## Execute

```
CALL GetOverdueTasks();
```

## Drop

```
DROP PROCEDURE procedure_name
```

## Parameters

- A parameter is one of three types

    1. IN: input
       - caller doesn't see the value set in the procedure
       - procedure sees the passed-in value
    2. OUT: output
       - caller sees the value set in the procedure
       - procedure doesn't see the passed-in value
    3. INOUT: input and output
        - caller sees the value set in the procedure
        - procedure sees the initial value
  
Example of creating a new task with a status_id:

```sql
DELIMITER //

CREATE PROCEDURE CreateTask(
    IN task_name VARCHAR(255),
    IN status_id INT UNSIGNED
    )
BEGIN
    INSERT INTO task(task_name, status_id)
    VALUES (task_name, status_id);
END //

DELIMITER ;
```

## Variables

- Two types of variables

    1. Local: used within a procedure and lost when procedure is done
    2. User-defined: persist throughout a session, across procedures
        - Indicated by an '@' name prefix

- ```DECLARE```d inside the BEGIN...END block as the first part of the block
  - Can be initialized using the ```DEFAULT``` keyword (otherwise starts as NULL)
- Assigned to using the ```SET``` command

Example of creating a new task with a status name:

```sql
DELIMITER //

CREATE PROCEDURE CreateTask(
    IN in_task VARCHAR(255),
    IN in_status VARCHAR(255)
    )
BEGIN
    DECLARE v_status_id INT UNSIGNED;
    
    SELECT status_id INTO v_status_id 
    FROM status 
    WHERE status_name = in_status;
    
    INSERT INTO task(task_name, status_id)
        VALUES (in_task, v_status_id);
END //

DELIMITER ;
```

## Get and List

```sql
SHOW PROCEDURE STATUS WHERE db = 'gtd_db';
```

```sql
SHOW CREATE PROCEDURE CreateTask;
```

## IF

```sql
...
IF condition1 THEN
    statements;
ELSIF condition2 THEN
    statements;
...
ELSE
    statements;
END IF;
...
```

Example of creating a new task with a specified status name (defaults to 'Next') and an optionally specified project name:

```sql
DELIMITER //

CREATE PROCEDURE CreateTask(
    IN in_task VARCHAR(255),
    IN in_status VARCHAR(255),
    IN in_project VARCHAR(255)
    )
BEGIN
    DECLARE v_status_id INT UNSIGNED;
    DECLARE v_proj_id INT UNSIGNED;

    # Determine v_status_id (default to 'Next' status id)
    IF in_status IS NULL THEN
        SET in_status = 'Next';
    END IF;
    SELECT status_id INTO v_status_id 
        FROM status 
        WHERE status_name = in_status;

    # Determine v_proj_id (default to NULL)
    IF in_project IS NOT NULL THEN
        SELECT proj_id INTO v_proj_id 
            FROM project 
            WHERE proj_name = in_project;
    END IF;
    
    INSERT INTO task(task_name, status_id, proj_id)
        VALUES (in_task, v_status_id, v_proj_id);
END //

DELIMITER ;
```

## LOOP

```sql
[begin_label:] LOOP
    statements;
END LOOP [end_label]
```

Let's work through an example.

- Create a ```calendar``` table

    ```sql
    CREATE TABLE calendar (
        date DATE PRIMARY KEY,
        month INT NOT NULL,
        quarter INT NOT NULL,
        year INT NOT NULL
    );
    ```

- Define a ```fillDates``` stored procedure to fill the calendar with dates between a startDate (inclusive) and an endDate (inclusive)

    ```sql
    DELIMITER //

    CREATE PROCEDURE fillDates(
        IN startDate DATE,
        IN endDate DATE
    )
    BEGIN
        DECLARE currentDate DATE DEFAULT startDate;
        
        insert_date: LOOP
            -- leave the loop if the current date is after the end date
            IF currentDate > endDate THEN
                LEAVE insert_date;
            END IF;
            
            -- insert date into the table
            INSERT INTO calendar(date, month, quarter, year)
            VALUES(currentDate, MONTH(currentDate), QUARTER(currentDate), YEAR(currentDate));

            -- increase date by one day
            SET currentDate = DATE_ADD(currentDate, INTERVAL 1 DAY);
            
        END LOOP;
    END //

    DELIMITER ;
    ```

- There's also a ```WHILE``` loop and a ```REPEAT``` loop, but let's just stick to the ```LOOP``` loop, for simplicity

## Cursors

- **Cursor** is a database object used for iterating the result of a ```SELECT``` statement
- Typically used in stored procedures, triggers, and functions where you need to *process individual rows returned by a query one at a time*
- Here's the basic syntax:

    ```sql
    -- declare a cursor
    DECLARE cursor_name CURSOR FOR 
    SELECT column1, column2 
    FROM your_table 
    WHERE your_condition;

    -- declare NOT FOUND handler
	DECLARE CONTINUE HANDLER 
        FOR NOT FOUND SET done = true;  

    -- open the cursor
    OPEN cursor_name;

    FETCH cursor_name INTO variable1, variable2;
    -- process the data

    -- close the cursor
    CLOSE cursor_name;
    ```

- For example list all the tasks in the gtd_db task table with the status_id and proj_id replaced by the status_name and proj_name.
  - This example uses several additional constructs like the use of a temp table to store the output results

    ```sql
    DELIMITER //

    CREATE PROCEDURE ListTasks()
    BEGIN
        DECLARE v_done BOOL DEFAULT false;
	    DECLARE v_task_name VARCHAR(255);
        DECLARE v_status_id INT UNSIGNED;
        DECLARE v_status_name VARCHAR(255);
        DECLARE v_proj_id INT UNSIGNED;
        DECLARE v_proj_name VARCHAR(255);

        -- declare a cursor
        DECLARE cursor_all_tasks CURSOR FOR 
            SELECT task_name, status_id, proj_id 
            FROM task;

        -- declare NOT FOUND handler
	    DECLARE CONTINUE HANDLER 
            FOR NOT FOUND SET v_done = true;  

        -- create a temp table to store the output
        CREATE TEMPORARY TABLE result(
            task_name VARCHAR(255),
            status_name VARCHAR(255),
            proj_name VARCHAR(255)
        );

        -- open the cursor
        OPEN cursor_all_tasks;

        -- loop over the tasks
        process_task: LOOP
            FETCH cursor_all_tasks INTO v_task_name, v_status_id, v_proj_id;
            IF v_done = true THEN
                LEAVE process_task;
            END IF;

            -- fetch status name
            IF v_status_id IS NOT NULL THEN
                SELECT status_name INTO v_status_name
                    FROM status WHERE status_id = v_status_id;
            ELSE
                SET v_status_name = NULL;
            END IF;
            
            -- fetch project name
            IF v_proj_id IS NOT NULL THEN
               SELECT proj_name INTO v_proj_name
                    FROM project WHERE proj_id = v_proj_id;
            ELSE
                SET v_proj_name = NULL;
            END IF;

            INSERT INTO result
                VALUES (v_task_name, v_status_name, v_proj_name);
            
        END LOOP;
        CLOSE cursor_all_tasks;

        SELECT * FROM result;
        DROP TEMPORARY TABLE result;
    END //

    DELIMITER ;
    ```

---
