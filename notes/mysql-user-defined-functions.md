# MySQL: User Defined Functions

- A user defined function (**UDF**) is like a stored procedure except that it returns a value

```sq
CREATE FUNCTION function_name (IN argument data-type, …) RETURNS data-type

BEGIN

    Procedure SQL statements;
    …
    RETURN (value or expression);
END;
```

- UDFs can be invoked from within stored procedures, triggers, and of course from SQL statements

---