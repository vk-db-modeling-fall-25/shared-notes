# MySQL: Manipulating Data

## Adding Rows

- If all attributes are specified:

  ```sql
  INSERT INTO table_name
    VALUES(value1, value2, ...),
    VALUES(...)
    ...;
  ```

- If some attributes can be NULL

  - Specify ```NULL``` for tha attribute value
  
    ```sql
    INSERT INTO table_name
      VALUES(value1, value2, ...),
      VALUES(...)
      ...;
    ```

  - Or specify a subset of columns and let the unspecified columns get NULL
  
    ```sql
    INSERT INTO table_name(column3, coulumn5)
      VALUES(value3, value5),
      VALUES(...)
      ...;
    ```

## Updating Rows

```sql
UPDATE table_name
SET column1 = expression1, column2 = expression2, ...
WHERE conditions;
```

Example from the GTD model to rename the status 'Next' to 'NextActions':

```sql
UPDATE status
SET status_name = 'NextActions'
WHERE status_name = 'Next';
```

MySQL "safe mode" requires a WHERE clause as a safety measure.

## Deleting Rows

```sql
DELETE FROM table_name
WHERE conditions...;
```

Example from the GTD model to delete the status 'Someday':

```sql
DELETE FROM status
WHERE status_name = 'Someday';
```

MySQL "safe mode" requires a WHERE clause as a safety measure.

## Commiting/Rolling-back Changes

- A **transaction** is a sequence of one or more statements that are executed atomically (all or nothing)
  - Required to maintain data integrity when modifying related pieces of data or when multiple users are concurrently modifying the same data
  - We'll study transaction management and concurrency in more details later in this course
- By default MySQL automatically wraps every statement you execute into a transaction and saves the changes automatically
  - Can disable this with ```SET autocommit = OFF;```
- To explicitly execute a sequence of statements within a transaction
  - START TRANSACTION;
  - Execute statements
  - One of
    - COMMIT; # saves changes
    - ROLLBACK; # aborts changes
  
### Example

1. Set up sample table
  
    ```sql
    CREATE DATABASE banks;

    USE banks;

    CREATE TABLE users (
        id INT PRIMARY KEY,
        username VARCHAR(255) NOT NULL,
        email VARCHAR(255)
    );
    ```

1. Commit changes

    ```sql
    START TRANSACTION;

    INSERT INTO users (id, username) 
    VALUES (1, 'john');


    UPDATE users 
    SET email = 'john.doe@example.com' 
    WHERE id = 1;

    SELECT * FROM users; # this will see the in-progress changes

    COMMIT;
    ```

1. Rollback changes

    ```sql
    START TRANSACTION;

    INSERT INTO users (id, username) 
    VALUES (2, 'jane');


    UPDATE users 
    SET email = 'jane.doe@example.com' 
    WHERE id = 2;

    ROLLBACK;

    SELECT * FROM users;
    ```

---