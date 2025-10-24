# MySQL: Altering Tables

## Overview
- All changes in the table structure are made by using the ```ALTER TABLE``` command followed by
  - ```ADD```
  - ```MODIFY```
  - ```DROP```

Example table:

```sql
CREATE TABLE vehicle (
    vehicle_id INT,
    year INT NOT NULL,
    make VARCHAR(100) NOT NULL,
    PRIMARY KEY(vehicle_id)
);
```

## Add Column

- e.g. add a ```model``` column to the ```vehicle``` table

```sql
ALTER TABLE vehicle
ADD model VARCHAR(100) NOT NULL;
```

- e.g. add ```color``` and ```note``` to the ```vehicle``` table

```sql
ALTER TABLE vehicle
ADD color VARCHAR(50),
ADD note VARCHAR(255);
```

## Modify Column

- e.g. Change ```note``` column to be mandatory with a max of 100 chars

```sql
ALTER TABLE vehicle
MODIFY note VARCHAR(100) NOT NULL; 
```

- Can specify multiple MODIFY expressions (just like in ADD)
- Depending on the DBMS there may be restrictions on modifications

## Rename Column

- e.g. rename ```note``` to ```vehicle_condition```

```sql
ALTER TABLE vehicle
CHANGE COLUMN note vehicle_condition VARCHAR(100) NOT NULL;
```

## Drop Column

```sql
ALTER TABLE vehicle
DROP COLUMN vehicle_condition;
```

> [!CAUTION]
> This deletes data

## Add Constraints

```sql
ALTER TABLE table_name
ADD PRIMARY KEY (...) ....
```

```sql
ALTER TABLE table_name
ADD FOREIGN KEY (...) ....
```

```sql
ALTER TABLE table_name
ADD CHECK (...)
```

## Delete Table

```sql
DROP TABLE table_name;
```

> [!CAUTION]
> This deletes data

- DBMS will enforce referential integrity constraints
- So, for example, can't drop a parent table that has FKs in child tables

---