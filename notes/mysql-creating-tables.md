# MySQL: Creating Tables

## Recommended Setup

- Write SQL commands in a script file with a `.sql` suffix
- Make commands idempotent e.g. with `IF NOT EXISTS` checks
- Execute script in mysql shell

    ```
    mysql -u root -v
    mysql> SOURCE /path/to/your/script.sql;
    ```

## Creating DB

```sql
SHOW DATABASES;
CREATE DATABASE IF NOT EXISTS gtd_db;
USE gtd_db;
SELECT database(); # shows selected database
```

## Creating Tables

Basic syntax:

```sql
CREATE TABLE table_name(
    column1 datatype constraints,
    column2 datatype constraints,
    ...
    PRIMARY KEY ...,
    FOREIGN KEY ...,
    CONSTRAINT ...
);
```

### Data Types

- A table contains multiple columns (attributes) each with a specific data type (the attribute domain)
- Here we'll cover the most common data types
  - See https://www.mysqltutorial.org/mysql-basics/mysql-data-types/ for more details on all the available data types

- Common numeric types
  - `INT`: 4-bytes signed integer ranging from $-2^{31}$ to $2^{31} - 1$. There are `SMALLINT` and `BIGINT` variants
  - `INT UNSIGNED`: 4-bytes unsigned integer ranging from $0$ to $2^{32} - 1$
  - `DECIMAL(P, D)`: P(recision) ranges from 1 to 65 and specifies the number of significant digits. D(ecimal digits) ranges from 0 to 30 and specifies the number of digits after the decimal point. D must be <= P. Used for monetary types etc.
  - `BOOLEAN`: Really a TINYINT i.e. a 1-byte signed/unsigned integer 
- Common string types
  - `CHAR(L)`: Fixed-length character string stored as exactly L characters (L <= 255). Used for area code (L = 3), etc.
  - `VARCHAR(L)`: Variable-length character string stored as up to L characters (L <= 255). Used for person names etc. 
- Common date types
  - `DATE`: A date value in CCYY-MM-DD format
  - `TIME`: A time value in hh:mm:ss format
  - `DATETIME`: A date and time value in CCYY-MM-DD hh:mm:ss format

### Creation Order
- First create tables that don't contain any foreign keys to other tables.
- Why? So that any foreign key referential integrity constraints are not violated.
- Two such tables in example model: PROJECT and STATUS

### Examples

Create `status` table:

```sql
# Option1
CREATE TABLE IF NOT EXISTS status(
    status_id INT UNSIGNED PRIMARY KEY,
    status_name VARCHAR(255) NOT NULL
);

# Option 2 (useful when PK is composite)
CREATE TABLE IF NOT EXISTS status(
    status_id INT UNSIGNED,
    status_name VARCHAR(255) NOT NULL,
    PRIMARY KEY(status_id)
);

DESCRIBE status;
```

Want the DB to pick a unique status_id for you?

```sql
CREATE TABLE status(
    status_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    status_name VARCHAR(255) NOT NULL
);
```

Similarly create the project table.

```sql
CREATE TABLE IF NOT EXISTS project(
    proj_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    proj_name VARCHAR(255) NOT NULL
);
```

Finally create the TASK table:

```sql
CREATE TABLE task(
    task_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(255) NOT NULL,
    task_due DATE,
    status_id INT UNSIGNED NOT NULL,
    proj_id INT UNSIGNED,
    FOREIGN KEY(status_id)
        REFERENCES status(status_id),
    FOREIGN KEY(proj_id)
        REFERENCES project(proj_id)
);
```

- Note the FK specification to enforce referential integrity (more on this later)

See all tables in DB:

```sql
SHOW TABLES;
```

## Populating Tables

Basic syntax:

```sql
INSERT INTO table_name(column_list)
VALUES
    (value_list_1),
    (value_list_2),
    ...
    (value_list_n);
```

- Again start with the parent tables (the ones without any FKs)

### Examples

```sql
INSERT INTO status(status_name)
VALUES
    ('Next'),
    ('Waiting'),
    ('Scheduled'),
    ('Someday');

SELECT * FROM status;
```

- Note that there's no need to specify the status_id if it is set to AUTO_INCREMENT

```sql
INSERT INTO project(proj_name)
VALUES
    ('Teach CSD 331'),
    ('Teach CS 143'),
    ('Get better at pickleball'),
    ('Plan summer vacation');
```

```sql
INSERT INTO task(task_name, task_due, status_id, proj_id)
VALUES
    ('Buy milk', NULL, 1, NULL),
    ('Prep next quiz', '2025-04-15', 3, 1),
    ('Prep next quiz', '2025-04-18', 3, 2),
    ('Amazon package', NULL, 2, 3);
```

- We'll learn later how to make it easier to populate the FKs.

## Constraints

When creating a table you can specify constraints on columns.

Types of constraints:
1. `PRIMARY KEY`
2. `FOREIGN KEY`
3. `UNIQUE`
4. `NOT NULL`
5. `DEFAULT`
6. `CHECK`

### PRIMARY KEY

- This constraint specifies the column(s) that uniquely identifies each row in the table
- The constraint automatically does the following:
  - makes every PK column *non-optional*
  - makes the PK *unique*
  - *indexes* the PK 
    - so that it can be used to retrieve rows efficiently

Syntax:

- Specify as a column constraint (when the PK consists of one column)

    ```sql
    CREATE TABLE status(
        status_id INT UNSIGNED PRIMARY KEY,
        status_name VARCHAR(255) NOT NULL
    );
    ```

- Specify as a table constraint (especially when the PK consists of more than one column)

    ```sql
    CREATE TABLE status(
        status_id INT UNSIGNED,
        status_name VARCHAR(255) NOT NULL,
        PRIMARY KEY(status_id)
    );
    ```

- Add a PK to a table that doesn't have one defined

    ```sql
    ALTER TABLE table_name
    ADD PRIMARY KEY(column1, column2, ...);
    ```

- Remove a PK from a table (though don't!)

    ```sql
    ALTER TABLE table_name
    DROP PRIMARY KEY;
    ```

### FOREIGN KEY

- This constraint specifies that a column or column-set is a primary key of another related table (the parent table)
- If the FK is specified then it must refer to an existing row in the parent table (referential integrity)
  - The DBMS enforces this!

Syntax: 

- As a table constraint in a CREATE TABLE or ALTER TABLE statement

    ```sql
    [CONSTRAINT constraint_name]
        FOREIGN KEY [foreign_key_name] (column_name, ...)
        REFERENCES parent_table(colunm_name,...)
        [ON DELETE reference_option]
        [ON UPDATE reference_option]
    ```

    - The optional constraint_name and foreign_key_name are generated by MySQL automatically if you don't specify them
    - Reference options
      - ```CASCADE```: propagate parent row change to related child rows
        - e.g. for an existence-dependent child entity
      - ```SET NULL```: set related child rows FK to null on parent row change
        - e.g. for an optional parent relationship
      - ```RESTRICT```: don't allow changes on related parent rows
        - This is the default action 

#### Examples

1. Inserting a non-existing FK fails

    ```sql
    INSERT INTO task(task_name, status_id) 
    VALUES('play', 100);
    ```

2. Since our initial definition of the `task` table did not specify reference options, it defaults to RESTRICT

    - Deleting a parent row fails

        ```sql
        DELETE FROM status
        WHERE status_id = 1;
        ```

    - Updating a parent row PK fails

        ```sql
        UPDATE status
        SET status_id = 100
        WHERE status_id = 1;
        ```
        
        - But updating a non-key column in the parent row is ok
  
            ```sql
            UPDATE status
            SET status_name = 'NextAction' 
            WHERE status_id = 1;
            ```

1. Modify the ```task``` table proj_id FK constraint to SET NULL on delete (after all a project is optional to a task)

    ```sql
    SHOW CREATE TABLE task;
    
    ALTER TABLE task
    DROP FOREIGN KEY `task_ibfk_2`;
    
    ALTER TABLE task 
    ADD CONSTRAINT `task_ibfk_2`
        FOREIGN KEY (proj_id) REFERENCES project(proj_id)
            ON DELETE SET NULL;
    ```

    - Now you're allowed to delete a project and any tasks associated with it will set the proj_id FK to null:


        ```sql
        DELETE FROM project
        WHERE proj_id = 2;

        SELECT * from task;
        ```
1. Modify the `task` table proj_id FK constraint to CASCADE on update (so that changes to proj_id get propagated)

    ```sql
    ALTER TABLE task 
    DROP FOREIGN KEY `task_ibfk_2`;

    ALTER TABLE task
    ADD CONSTRAINT `task_ibfk_2`
        FOREIGN KEY (proj_id) REFERENCES project(proj_id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;
    ```

    - Now you're allowed to update a proj_id and any tasks associated with it will update as well:

        ```sql
        UPDATE project
        SET proj_id = 6
        WHERE proj_name = 'Teach CSD 331';

        SELECT * FROM project;

        SELECT * FROM task;
        ```

### UNIQUE

- This constraint specifies that a column or column-set must have unique values in the table (so no duplicates allowed)
- Under the hood an index is used to enforce the uniqueness
- NULL values (if allowed) are treated as unique
  - So multiple NULL values can be inserted into a UNIQUE column

Syntax:

1. As a column constraint

    ```sql
    ...
    phone VARCHAR(15) UNIQUE,
    ...
    ```

2. As a table constraint (especially for column-set uniqueness)

    ```sql
    ...
    CONSTRAINT c_name_address UNIQUE (name, address)
    ...
    ```

### NOT NULL

- This constraint specifies that a column must contain a value
- It is specified as a ```NOT NULL``` column constraint
- It is used for mandatory attributes

### DEFAULT

- This constraint allows you to specify a default value for a column
- It is specified as a ```DEFAULT default_value``` column constraint
- If an insert or update does not specify a value for the column, it uses the default value

### CHECK

- This constraint specifies that a boolean expression must not be false for any row of the table
- It can be specified as a column constraint

    ```sql
    ...
    cost DECIMAL(10, 2) NOT NULL CHECK (cost >= 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    ...
    ```

- Or it can be specified as a table constraint (especially when relating mutliple columns)

    ```sql
    ...
    CONSTRAINT chk_price_gt_cost
        CHECK (price > cost),
    ...
    ```

## Indexes

- Used to:
  - More efficient retrieval based on indexed attributes
  - Enforce uniqueness (e.g. for a UNIQUE constraint on a column or column-set)
- Syntax

    ```sql
    CREATE [UNIQUE] INDEX index_name ON table_name(column1, column2, ...)
    ``` 
- Example

  - Want to be able to sort ```task``` records by due date

    ```sql
    CREATE INDEX idx_task_due_date ON task(task_due);

    SHOW CREATE TABLE task;
    ```

  - Want to be able to search ```task``` record by task_name

    ```sql
    CREATE INDEX idx_task_name ON task(task_name);
    ```
