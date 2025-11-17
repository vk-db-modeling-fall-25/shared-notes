# Transactions

## Overview

> A **transaction (txn)** is a logical unit of work, composed of one or more database requests, that must be entirely completed or entirely aborted; no intermediate states are acceptable

Example:

- A bank database of accounts with balances

    ```sql
    CREATE DATABASE bank;
    USE bank;
    CREATE TABLE account (
        id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(255) NOT NULL,
        balance DECIMAL(10, 2) NOT NULL
    );
    INSERT INTO account (username, balance) 
        VALUES('john', 100.10),
        VALUES('jane', 617.50);
    ```

- Transfer money between two accounts **transactionally**

    ```sql
    START TRANSACTION;

    UPDATE account
    SET Balance = Balance - 10
    WHERE username = 'john';

    UPDATE account
    SET Balance = Balance + 10
    WHERE username = 'jane';

    COMMIT;

    SELECT * FROM account;
    ```

## ACID

> [!IMPORTANT]
> Each transaction must have the **ACID** properties

- **Atomicity**
- **Consistency**
- **Isolation**
- **Durablity**

### Atomicity

Example scenario:
1. Txn starts
2. Some, but not all, requests in txn complete
3. Database crashes before txn commits
4. Database comes back up in a pre-txn state

> Atomicity: either all database requests in a transaction complete or none complete

- Single, indivisible, logical unit of work
- Often required for logical consistency
  - e.g. money transfer between two accounts must be atomic (otherwise there will be missing money)
- Required for data consistency (as we'll see next)

### Consistency

Example scenario:
1. Database is in a consistent state
     - No violations of entity integrity, referential integrity, constraints, etc.
2. Txn starts
3. Request 1 deletes a parent row 
4. But txn has no request to delete an associated child row with the child entity being existence-dependent
5. Database fails the txn commit due to referential integrity violation

> Consistency: when a transaction completes the database must be in a consistent state (entity integrity, referential integrity, constraints, etc.)

- A single request in a transaction may violate constraints as long as the whole transaction does not
  - e.g. deleting a parent row and then cascading the delete to its children rows

### Isolation

Example scenario:
1. Two txns (txn1 and txn2) are concurrently in progress i.e. both started but not completed
2. There's a row in a table with an attribute whose value is 6
3. txn1 executes an update request to increment that attribute from 6 to 7
4. Then txn2 also executes an update request to increment that attribute from 6 to 7
   - Why 6 to 7 and not 7 to 8? Because of isolation between txns
   - Why isolate? In case tx1 fails or is rolled back
1. txn1 commits successfully
2. txn2 attempts to commit and encounters an error denoting concurrent modification
3. txn2 retries, now sees the value as 7 (from the completed txn1), increments it to 8, and commits successfully

> Isolation: data changes made by an in-progress transaction are not visible to another transaction until the first transaction completes

- Particularly critical in multi-user DB environments
- Note that in principle there are different levels of isolation, but we'll consider only the strongest form (which is also the most common and useful form) i.e. in-flight changes of a txn are not visible to any other txn until the first txn completes
  - Suppose txns T1, T2, T3 are executing concurrently
  - DBMS can execute each one serially, but that would be too slow
  - So DBMS carefully schedules a concurrent execution of the txns
  - **Serializability**: the concurrent execution schedule yields the same result as if the txns executed serially
- A single user DB environment automatically has isolation and serializability due to the absence of concurrent txns!

### Durability

Example scenario:
1. A txn commits successfully
2. The data changed by the txn are waiting in an in-memory buffer to be flushed to disk
3. The DB crashes
4. When the DB recovers, the txn is indeed still committed (its effects are visible)

> Durability: when a transaction is committed, it cannot be undone or lost, even in the event of a system failure

- Of course, durability can't be guaranteed in the face of "catastrophic failures"
  - Need a robust backup strategy
    - **Failure-domain** diversity
    - Low **recovery-point** (RP): how long before a write is backed up

## The Transaction Log

- How does a DBMS provide ACID guarantees?
- Using a **transaction log**

Example txn with two update statements:

| Log Seq Num | Transaction ID | Operation | Table Name | Old Value                                                                                                 | New Value                                                                                                      | Previous LSN | Next LSN |
| ----------- | -------------- | --------- | ---------- | --------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- | ------------ | -------- |
| 100         | 500            | BEGIN     | -          | -                                                                                                         | -                                                                                                              | NULL         | 101      |
| 101         | 500            | UPDATE    | Orders     | order_id: 10523<br>order_status: 'PENDING'<br>last_updated: '2025-10-29 14:15:22'<br>updated_by: 'system' | order_id: 10523<br>order_status: 'PROCESSING'<br>last_updated: '2025-10-29 15:23:45'<br>updated_by: 'app_user' | 100          | 102      |
| 102         | 500            | UPDATE    | Inventory  | SKU-12345: qty=150<br>SKU-12346: qty=200<br>SKU-12347: qty=75                                             | SKU-12345: qty=148<br>SKU-12346: qty=199<br>SKU-12347: qty=74                                                  | 101          | 103      |
| 103         | 500            | COMMIT    | -          | -                                                                                                         | -                                                                                                              | 102          | NULL     |

Database recovery:
- DBMS examines transaction log
- It rolls back all uncommitted or incomplete transactions
- It persists any committed transactions that were not previously flushed to persistent store

When is the txn log written to?
- **Write ahead logging (WAL)**: before executing the DB request
  - Every materialized change has a log entry
  - A log entry may or may not have a materialized change (depending on when a failure ocurred)
    - But that's OK
    - DBMS can reconcile the logs with the state of the DB 
- Mental model
  - Write down what you're about to do
  - Only then try and do it
- Adds overhead, but the benefit outweighs the cost

Where is the txn log stored?
- As one or more files managed separately from the DB files

## Concurrency Control

> **Concurrency control**: coordinating the simultaneous execution of transactions in a multiuser DB system, while maintaining ACID properties (particularly isolation) for each transaction

### Why Needed?

1. Lost updates
2. Uncommitted data
3. Inconsistent retrievals

#### Lost Updates

> When two concurrent transactions, T1 and T2, are updating the same data element and one of the updates is lost (overwritten by the other transaction)

Scenario:

Two transactions (T1 and T2) are both trying to update the same bank account balance. The initial balance is **$1000**.

- **Transaction T1:** Withdraw $200 (read balance, subtract 200, write new balance)
- **Transaction T2:** Deposit $300 (read balance, add 300, write new balance)

**Expected Final Balance:** $1100 (1000 - 200 + 300)

✅ Correct Serial Execution (T1 then T2):

| Time | Transaction T1             | Transaction T2             | Balance  |
| ---- | -------------------------- | -------------------------- | -------- |
| t0   |                            |                            | **1000** |
| t1   | BEGIN                      |                            | 1000     |
| t2   | READ(Balance) → 1000       |                            | 1000     |
| t3   | Balance = 1000 - 200 = 800 |                            | 1000     |
| t4   | WRITE(Balance = 800)       |                            | **800**  |
| t5   | COMMIT                     |                            | 800      |
| t6   |                            | BEGIN                      | 800      |
| t7   |                            | READ(Balance) → 800        | 800      |
| t8   |                            | Balance = 800 + 300 = 1100 | 800      |
| t9   |                            | WRITE(Balance = 1100)      | **1100** |
| t10  |                            | COMMIT                     | 1100     |

**Final Balance: $1100** ✓ Correct

❌ Incorrect Interleaved Execution (Lost Update):

| Time | Transaction T1             | Transaction T2              | Balance  |
| ---- | -------------------------- | --------------------------- | -------- |
| t0   |                            |                             | **1000** |
| t1   | BEGIN                      |                             | 1000     |
| t2   | READ(Balance) → 1000       |                             | 1000     |
| t3   |                            | BEGIN                       | 1000     |
| t4   |                            | READ(Balance) → 1000        | 1000     |
| t5   | Balance = 1000 - 200 = 800 |                             | 1000     |
| t6   |                            | Balance = 1000 + 300 = 1300 | 1000     |
| t7   | WRITE(Balance = 800)       |                             | **800**  |
| t8   | COMMIT                     |                             | 800      |
| t9   |                            | WRITE(Balance = 1300)       | **1300** |
| t10  |                            | COMMIT                      | 1300     |

**Final Balance: $1300** ✗ Incorrect


🔍 What Went Wrong?

The withdrawal of $200 by T1 is completely lost because T2 overwrote it with a value based on stale data.

#### Uncommitted Data (Dirty Read)

> Two concurrent transactions, T1 and T2, and T1 is rolled back after T2 has already accessed the uncommitted data from T1 (violating isolation)

Scenario:

Two transactions (T1 and T2) are accessing the same bank account. The initial balance is **$1000**.

- **Transaction T1:** Withdraw $500 (but will be rolled back due to an error)
- **Transaction T2:** Read the balance to check if sufficient funds exist for a $600 withdrawal

**Expected Behavior:** T2 should see balance as $1000 (since T1 is rolled back)

✅ Correct Serial Execution (T1 then T2):

| Time | Transaction T1             | Transaction T2          | Balance  |
| ---- | -------------------------- | ----------------------- | -------- |
| t0   |                            |                         | **1000** |
| t1   | BEGIN                      |                         | 1000     |
| t2   | READ(Balance) → 1000       |                         | 1000     |
| t3   | Balance = 1000 - 500 = 500 |                         | 1000     |
| t4   | WRITE(Balance = 500)       |                         | 500      |
| t5   | [Error occurs]             |                         | 500      |
| t6   | ROLLBACK                   |                         | **1000** |
| t7   |                            | BEGIN                   | 1000     |
| t8   |                            | READ(Balance) → 1000    | 1000     |
| t9   |                            | Check: 1000 ≥ 600? YES  | 1000     |
| t10  |                            | Proceed with withdrawal | 1000     |
| t11  |                            | COMMIT                  | 1000     |

**T2 reads: $1000** ✓ Correct (T1's changes were rolled back)

❌ Incorrect Interleaved Execution (Dirty Read):

| Time | Transaction T1             | Transaction T2       | Balance  |
| ---- | -------------------------- | -------------------- | -------- |
| t0   |                            |                      | **1000** |
| t1   | BEGIN                      |                      | 1000     |
| t2   | READ(Balance) → 1000       |                      | 1000     |
| t3   | Balance = 1000 - 500 = 500 |                      | 1000     |
| t4   | WRITE(Balance = 500)       |                      | **500**  |
| t5   |                            | BEGIN                | 500      |
| t6   |                            | READ(Balance) → 500  | 500      |
| t7   |                            | Check: 500 ≥ 600? NO | 500      |
| t8   |                            | Reject withdrawal    | 500      |
| t9   |                            | COMMIT               | 500      |
| t10  | [Error occurs]             |                      | 500      |
| t11  | ROLLBACK                   |                      | **1000** |

**T2 reads: $500** ✗ Incorrect (T2 read uncommitted data from T1)

🔍 What Went Wrong?

T2 incorrectly rejected a withdrawal that should have been allowed because it read uncommitted data that was later rolled back.

#### Inconsistent Retrievals

> A transactions accesses data before and after one or more other transactions finish working with that data

Scenario:

Two transactions are accessing bank accounts. T2 is calculating the total balance across all accounts.

- **Initial State:** Account A = $500, Account B = $300, Account C = $200 (Total = $1000)
- **Transaction T1:** Transfer $100 from Account A to Account C
- **Transaction T2:** Calculate sum of all account balances

**Expected Total:** $1000 (money is just moving between accounts)

✅ Correct Serial Execution (T1 then T2):

| Time | Transaction T1 | Transaction T2           | Account A | Account B | Account C |
| ---- | -------------- | ------------------------ | --------- | --------- | --------- |
| t0   |                |                          | **500**   | **300**   | **200**   |
| t1   | BEGIN          |                          | 500       | 300       | 200       |
| t2   | READ(A) → 500  |                          | 500       | 300       | 200       |
| t3   | WRITE(A = 400) |                          | **400**   | 300       | 200       |
| t4   | READ(C) → 200  |                          | 400       | 300       | 200       |
| t5   | WRITE(C = 300) |                          | 400       | 300       | **300**   |
| t6   | COMMIT         |                          | 400       | 300       | 300       |
| t7   |                | BEGIN                    | 400       | 300       | 300       |
| t8   |                | READ(A) → 400            | 400       | 300       | 300       |
| t9   |                | READ(B) → 300            | 400       | 300       | 300       |
| t10  |                | READ(C) → 300            | 400       | 300       | 300       |
| t11  |                | SUM = 400+300+300 = 1000 | 400       | 300       | 300       |
| t12  |                | COMMIT                   | 400       | 300       | 300       |

**T2 calculates: $1000** ✓ Correct

❌ Incorrect Interleaved Execution (Inconsistent Retrieval):

| Time | Transaction T1 | Transaction T2           | Account A | Account B | Account C |
| ---- | -------------- | ------------------------ | --------- | --------- | --------- |
| t0   |                |                          | **500**   | **300**   | **200**   |
| t1   |                | BEGIN                    | 500       | 300       | 200       |
| t2   |                | READ(A) → 500            | 500       | 300       | 200       |
| t3   |                | READ(B) → 300            | 500       | 300       | 200       |
| t4   | BEGIN          |                          | 500       | 300       | 200       |
| t5   | READ(A) → 500  |                          | 500       | 300       | 200       |
| t6   | WRITE(A = 400) |                          | **400**   | 300       | 200       |
| t7   | READ(C) → 200  |                          | 400       | 300       | 200       |
| t8   | WRITE(C = 300) |                          | 400       | 300       | **300**   |
| t9   | COMMIT         |                          | 400       | 300       | 300       |
| t10  |                | READ(C) → 300            | 400       | 300       | 300       |
| t11  |                | SUM = 500+300+300 = 1100 | 400       | 300       | 300       |
| t12  |                | COMMIT                   | 400       | 300       | 300       |

**T2 calculates: $1100** ✗ Incorrect

🔍 What Went Wrong?

T2 retrieved data in an inconsistent state - it saw the database "in the middle" of T1's transfer, reading some values before the transfer and some after.

### The Scheduler

We know that:
1. Problems can arise when transactions execute concurrently without proper concurrency controls
2. A txn involves a series of database requests that takes the database from one consistent state to another
3. Database consistency can only be ensured before and after the execution of a txn
  
     - Unavoidable temporary state of inconsistency if a transaction updates multiple tables or rows

Order of txn execution:
- Serial execution is too slow
- Interleaved execution is safe only if concurrent transactions access unrelated data
- What about transactions operate on related data or the same data?
- The **scheduler** is a special DBMS process that establishes the order in which the operations are executed within concurrent transactions
  - Interleaves the execution of operations to ensure isolation and serializability
  - How does it ensure?
  - Using concurrency control techniques
    1. Locking (pessimistic)
    2. Timestamping
    3. Optimistic
  - Also attempts to use CPU and storage resources efficiently (not leaving them idle when they could be executing pending operations)

Matrix of conflicting database operations:

|               |   **T2: Read**    | **T2: Write**  |
| ------------- | :---------------: | :------------: |
| **T1: Read**  | ✅ **No Conflict** | ⚠️ **Conflict** |
| **T1: Write** |  ⚠️ **Conflict**   | ⚠️ **Conflict** |

### Techniques

The most commonly used concurrency control techniques are:

1. Locking (pessimistic)
2. Timestamping
3. Optimistic

#### Locking (pessimistic)

> A **pessimistic lock** guarantees exclusive use of a data item to a transaction

- Why called "pessimistic"?
  - Based on the assumption that conflicting transactions are likely so preemptively guard with exclusive access (regardless of whether a conflicting transaction actually exists or not)
- DBMS has a **lock manager** which assigns and polices locks used by transactions
- T1 acquires locks prior to data access
- T1 releases locks when it completes
- For the duration of T1, no other transaction can access the locked data

Locking granularity:
- Database level: horrible!
  - Serializes all transactions
- Table level: better, but still bad
  - Serializes transactions that are accessing unrelated rows in a table
- Page level: not bad
  - A page is a directly addressable section of disk with a fixed size e.g. 4K
  - It is the unit of disk access
  - A page typically contains several rows in a table
  - So only transactions accessing the rows in a page are serialized
- Row level: good
  - Only transactions accessing the same row are serialized
  - But complex
- Field level: best
  - Only transactions accessing the same field in the same row are serialized
  - But most complex

Lock types:
1. Binary

     - Only two states: locked or unlocked
     - A binary lock provide exclusive access to data at the locking granularity to a single transaction and other transactions have to wait to acquire the lock
     - That needlessly penalizes two transactions that just want to read related data

2. Shared/Exclusive

    - *Exclusive* lock is like a binary lock and is used when a transaction mutates data

      ```sql
      START TRANSACTION;

      -- 1. LOCK ACQUIRED: Exclusive lock grabbed here
      SELECT stock_quantity FROM products WHERE product_id = 1 FOR UPDATE;

      -- 2. LOCK HELD: Still holding the lock during all operations
      UPDATE products SET stock_quantity = stock_quantity - 5 WHERE product_id = 1;
      INSERT INTO orders (product_id, quantity) VALUES (1, 5);

      -- 3. LOCK RELEASED: Lock released here
      COMMIT;  -- or ROLLBACK
      ```

      Transaction T1:
      
      ```sql
      START TRANSACTION;
      SELECT balance FROM accounts WHERE account_id = 101 FOR UPDATE;
      -- Lock acquired
      ```

      Transaction T2 (concurrent):
      ```sql
      START TRANSACTION;
      SELECT balance FROM accounts WHERE account_id = 101 FOR UPDATE;
      -- BLOCKS HERE - must wait for T1 to finish
      ```
  
    - *Shared* lock is a common lock that grants read-only access to multiple transactions
  
      ```sql
      -- FOR SHARE: "I'm reading, don't change it"
      START TRANSACTION;
      SELECT balance FROM accounts WHERE account_id = 101 FOR SHARE;
      -- Others can read with FOR SHARE, but cannot update

      -- FOR UPDATE: "I'm about to change this, stay away"
      START TRANSACTION;
      SELECT balance FROM accounts WHERE account_id = 101 FOR UPDATE;
      -- Nobody else can read with locks or write 
      ```

    - So three possible locking states
      1. Unlocked
      2. Shared (read)
      3. Exclusive (write)

      Lock Compatibility Matrix:

      |                        | T2: Regular SELECT | T2: FOR SHARE | T2: FOR UPDATE | T2: UPDATE/DELETE |
      | ---------------------- | ------------------ | ------------- | -------------- | ----------------- |
      | **T1: Regular SELECT** | ✅ Compatible       | ✅ Compatible  | ✅ Compatible   | ✅ Compatible      |
      | **T1: FOR SHARE**      | ✅ Compatible       | ✅ Compatible  | ❌ Blocks       | ❌ Blocks          |
      | **T1: FOR UPDATE**     | ✅ Compatible       | ❌ Blocks      | ❌ Blocks       | ❌ Blocks          |
      | **T1: UPDATE/DELETE**  | ✅ Compatible       | ❌ Blocks      | ❌ Blocks       | ❌ Blocks          |

Locking problems:

1. A schedule may not be serializable
   - e.g. if a transaction locks, then, unlocks, then locks the same data, it may lead to conflicting interleaved access to the same data across multiple concurrent transactions 
   - Handled via two-phase locking (not to be confused with two-phase commit used for distributed transactions)
      1. Growing phase: acquires all required locks without any unlocking
      2. Shrinking phase: releases all locks without and new locking
2. A schedule may create deadlocks
   1. Deadlock ocurrs when T1 is has lock A and is waiting for lock B while T2 has lock B and is waiting for lock A
   2. Scheduler uses deadlock detection and prevention techniques
      1. e.g. acquire locks in a determinstic order

#### Timestamping

> Assign a unique, monotonically increasing timestamp to each transaction 
>
> The timestamp order determines the execution order and is used to resolve conflicts

How does this work?
- Each value stored in the DB has two additional timestamp fields
  - Timestamp of the latest txn that read it
  - Timestamp of the latest txn that wrote to it
- If a txn accesses a data value that was accessed by a more recent txn, it aborts (rolls back) and retries with a new timestamp

Punting on details, since locking is a much more commonly used concurrency control mechanism.

#### Optimistic

- Assumption that the majority of operations don't conflict
- Txn is executed without restrictions
 until it's time to commit
- At commit time, detect if a conflicting commit has taken place while this txn was in-progress
  - If so, abort and retry
  - If not, commit
- Three phases of a txn:
  1. Read phase
     1. Updates are made to a private copy of the database values
  2. Validation phase
     1. Detect any conflicting changes committed
     2. If conflict detected, roll back and restart txn
  3. Write phase
     1. apply the changes to the database

Optimistic vs Pessimistic:

| Factor              | Optimistic (OCC)      | Pessimistic (Locks)   |
| ------------------- | --------------------- | --------------------- |
| **Low Contention**  | ✅ Higher throughput   | Lower (lock overhead) |
| **High Contention** | Lower (many retries)  | ✅ Better throughput   |
| **Read-Heavy**      | ✅ Much higher         | Lower (lock overhead) |
| **Write-Heavy**     | Depends on contention | ✅ Often better        |
| **Latency**         | Low (no waiting)      | Higher (blocking)     |
| **Wasted Work**     | High on conflicts     | None                  |

---