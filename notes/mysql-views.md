# MySQL: Views

## Overview

- Output of a `SELECT` query is a table
- A **view** is a virtual table based on a `SELECT` query
- The tables on which the view is based are called **base tables**
  - A view can act as a base table for another view!

## Creating a View

```sql
CREATE VIEW view_name [(column_list)]
AS SELECT query
```

Example from the GTD model:
- Create a view that shows all tasks that are overdue

    ```sql
    CREATE VIEW overdue_tasks AS
        SELECT task_name, task_due
        FROM task
        WHERE task_due IS NOT NULL 
            AND task_due < CURDATE();
    ```

## Properties of Views

- A view_name can be used anywhere in place of a table_name
- Views are *dynamically updated* i.e. the SELECT query is executed every time the view is invoked
- A view can be used:
  1. to control access to certain cols/rows in tables
  1. for commonly needed reports

---