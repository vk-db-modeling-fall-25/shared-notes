# MySQL: Procedural Overview

- SQL is a declarative language i.e. you only specify *what*
- It is not a procedural language i.e. you don't specify *how*
- SQL-99 standard defined the use of **persistent stored modules (PSM)**
  - Block of code containing standard SQL statements and procedural extensions
  - The block of code is stored and executed at the DBMS server
- Facilitates access-controlled sharing across multiple users
- MySQL uses a **procedural SQL** similar to the Oracle PL/SQL
- End users can use procedural SQL to create:
  - Stored procedures
  - Triggers
  - Procedural SQL functions

---