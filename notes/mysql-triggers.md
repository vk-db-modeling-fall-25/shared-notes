
# MySQL: Triggers

- Stored procedures can help encapsulate business logic
- But they must be manually executed
- **Triggers advance stored procedures by having the DBMS run them automatically under specified conditions**

Properties:
1. Invoked *before* or *after* a data row is mutated
    - MySQL only supports such row-level triggers
    - SQL standard also supports statement-level triggers i.e. a txn regardless of how many rows involved
2. Associated with one DB table
3. A table can have many triggers
4. Executed as part of the transaction that triggered it

Uses:
- Enforce constraints
- Enforce referential integrity
- Auditing e.g. mutation history

Syntax:
```sql
CREATE TRIGGER trigger_name
{BEFORE | AFTER} {INSERT | UPDATE | DELETE}
ON table_name
FOR EACH ROW
BEGIN
    -- Trigger body (SQL statements)
END;
```

- ```OLD``` and ```NEW``` modifiers distinguish between before mutation and after mutation values
- ```BEFORE``` is typically used to abort a mutation based on some logic
- ```AFTER``` is typically used to update dependent data (e.g. stats) based on the mutation
- Can create multiple triggers for the same event on a table (e.g. AFTER UPDATE) by specifying ```{FOLLOWS|PRECEDES} existing_trigger_name``` after ```FOR EACH ROW```
- Can call a stored procedure from a Trigger using the ```CALL``` statement
  - Requires that the stored procedure has no OUT or INOUT params

## Example
1. Create an ```items``` table

    ```sql
    CREATE TABLE items (
        id INT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        price DECIMAL(10, 2) NOT NULL
    );
    ```

1. Insert a row

    ```sql
    INSERT INTO items(id, name, price) 
    VALUES (1, 'Item', 50.00);
    ```

2. Create ```item_changes``` table to store mutation history (1:M relationship)

    ```sql
    CREATE TABLE item_changes (
        change_id INT PRIMARY KEY AUTO_INCREMENT,
        item_id INT,
        change_type VARCHAR(10),
        change_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (item_id) REFERENCES items(id)
    );
    ```

3. Create ```update_items_trigger```

    ```sql
    DELIMITER //

    CREATE TRIGGER update_items_trigger
    AFTER UPDATE
    ON items
    FOR EACH ROW
    BEGIN
        INSERT INTO item_changes (item_id, change_type)
        VALUES (NEW.id, 'UPDATE');
    END;
    //

    DELIMITER ;
    ```

4. Update a row in ```items````

    ```sql
    UPDATE items
    SET price = 60.00 
    WHERE id = 1;
    ```

5. See logged changes

    ```sql
    SELECT * FROM item_changes;
    ```

## Caution

- Triggers susceptible to multiple SQL commands operating on the same rows at the same time in the same transaction

Example:
- A PRODUCT table whose attributes include
  - P_QOH: quantity-on-hand
  - P_MIN: min qty-on-hand to trigger a re-order
  - P_REORDER: whether to order more qty
- Create a trigger on PRODUCT updates to set P_REORDER as needed
- Here's something that **doesn't work** (compiles but shows error when run):

    ```sql
    DELIMITER //
    CREATE TRIGGER TRG_PRODUCT_REORDER
    AFTER UPDATE ON PRODUCT
    FOR EACH ROW
    BEGIN
        UPDATE PRODUCT
        SET P_REORDER = 1
        WHERE P_QOH <= P_MIN;
    END
    //
    DELIMITER ;

    START TRANSACTION
    UPDATE PRODUCT SET P_QOH = 2 WHERE P_CODE = '6';
    SELECT * FROM PRODUCT;
    ROLLBACK;
    ```
- Why doesn't it work?
  - The triggering statement updates P_CODE 6
  - Then pauses to run the trigger
  - The trigger attempts to update the same P_CODE 6 row (to set P_REORDER)
    - But this is blocked by the in-progress update to the row from the triggering statement
    - Only one update statement can be operating on a row at a time
- So what works?

    ```sql
    DELIMITER //
    CREATE TRIGGER TRG_PRODUCT_REORDER
    BEFORE UPDATE ON PRODUCT
    FOR EACH ROW
    BEGIN
        IF NEW.P_QOH <= NEW.P_MIN THEN
            SET NEW.P_REORDER = 1;
        END IF;
    END
    //
    DELIMITER ;

    START TRANSACTION
    UPDATE PRODUCT SET P_QOH = 2 WHERE P_CODE = '6';
    SELECT * FROM PRODUCT;
    ROLLBACK;
    ```

    - DBMS makes two copies of every row being changed by a DML (INSERT/UPDATE/DELETE)
      - OLD (values before any changes)
      - NEW (values after changes)
    - NEW and OLD values can be referenced only within a trigger action
    - Trigger executes BEFORE instead of AFTER update
      - This means that the trigger action is performed before the triggering statement has been saved to disk but after the triggering statement changes are made in memory
    - Trigger uses the NEW reference to assign to P_REORDER before the UPDATE is saved to disk
    - This new trigger does not use any DML statements!
      - It manipulates the values in the row that the triggering statement paused on while those values are still in memory for the triggering statement

- There's another unresolved issue
  - What about resetting P_REORDER when we have enough qty-on-hand?
  - Ok, fix it like so:

    ```sql
    BEGIN
        IF NEW.P_QOH <= NEW.P_MIN THEN
            SET NEW.P_REORDER = 1;
        ELSE
            SET NEW.P_REORDER = 0;
        END IF;
    END
    ```

- Aargh, there's one final unresolved issue
  - What about a spurious INSERT that sets an inconsistent P_REORDER?
  - Need the same trigger on both INSERT and UPDATE
  - Use a stored PROCEDURE to eliminate code duplication
    - The procedure uses variables and cannot reference NEW and OLD

    ```sql
    DELIMITER //
    CREATE PROCEDURE PRC_PRODUCT_REORDER(IN PQOH INT, IN PMIN INT, OUT PREORDER INT)
    BEGIN
        IF PQOH <= PMIN THEN
            SET PREORDER = 1;
        ELSE
            SET PREORDER = 0;
        END IF;
    END;
    //
    DELIMITER ;
    ```

    ```sql
    DELIMITER //
    CREATE TRIGGER TRG_UPDATE_PRODUCT_REORDER
    BEFORE UPDATE ON PRODUCT
    FOR EACH ROW
    BEGIN
        CALL PRC_PRODUCT_REORDER(NEW.P_QOH, NEW.P_MIN, NEW.P_REORDER);
    END;
    //
    DELIMITER ;
    ```

    ```sql
    DELIMITER //
    CREATE TRIGGER TRG_INSERT_PRODUCT_REORDER
    BEFORE INSERT ON PRODUCT
    FOR EACH ROW
    BEGIN
        CALL PRC_PRODUCT_REORDER(NEW.P_QOH, NEW.P_MIN, NEW.P_REORDER);
    END;
    //
    DELIMITER ;
    ```

### Other Statements

- ```DROP TRIGGER trigger_name;```
- ```SHOW TRIGGERS;```

---