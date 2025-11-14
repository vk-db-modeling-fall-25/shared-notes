# MySQL: User Defined Functions

- A user defined function (**UDF**) is like a stored procedure except that it returns a value

```sql
CREATE FUNCTION function_name (argument data-type, …)
RETURNS data-type
[DETERMINISTIC | NOT DETERMINISTIC]
BEGIN

    Procedure SQL statements;
    …
    RETURN (value or expression);
END;
```

- UDFs can be invoked from within stored procedures, triggers, and of course from SQL statements

## Compared with Stored Procedure

- Like a Stored Procedure
  - Encapsulates reusable logic
  - Stored and executed at the DB
- Unlike a Stored Procedure
  - Returns a single value (vs multiple OUT params)
  - Can be used in SQL statements (vs CALL statement)
  - Can't use COMMIT/ROLLBACK

## Determinism

```sql
DETERMINISTIC         -- Always returns same output for same input
NOT DETERMINISTIC     -- May return different output (uses NOW(), RAND(), etc.)
```

Example:
```sql
-- DETERMINISTIC - same inputs always give same output
CREATE FUNCTION calculate_tax(price DECIMAL(10,2))
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    RETURN price * 0.08;  -- Always returns same result for same price
END;

-- NOT DETERMINISTIC - output varies
CREATE FUNCTION get_greeting()
RETURNS VARCHAR(100)
NOT DETERMINISTIC
BEGIN
    IF HOUR(NOW()) < 12 THEN
        RETURN 'Good morning';
    ELSE
        RETURN 'Good afternoon';
    END IF;
END;
```

Why does this need to be specified?
- For data consistency in replicated databases
  - Statement-based replication (SBR) replays SQL statements
    - Only deterministic functions are safe for SBR
    - Non-deterministic functions instead uses row-based replication (RBR) 

---