# NoSQL Databases

## Overview

- We understand the challenges of Big Data for relational DBs
- We know how distributed file systems address some of those challenges
- We know how map-reduce facilitates the processing of Big Data
  - using distributed full-scans programmed as ```map``` and ```reduce``` phases
- But we still miss the convenience and functionality of a database
  - e.g. to maintain structured or semi-structured data in our application, without the full overhead of a relational DB (thereby surmounting the Big Data challenges)
- Is there any hope?
- Yes! In the past 20 years several Big Data databases (aka NoSQL because they're not relational) have emerged from Big tech (Google, Facebook, etc.)

- There are four main types of these (so far):
    1. Key-value e.g. Google's BigTable (which uses GFS as its storage layer)
    2. Document e.g. MongoDB
    3. Column-oriented e.g. Facebook's Cassandra
    4. Graph e.g. Neo4j
- Let's take a brief look at each type

## Key-Value

> A NoSQL database that stores data as a collection of key-value pairs
>
> The key acts as an identifier for the value
>
> The value is opaque to the database (it can be anything and the database does not try to interpret it or index it or whatever)

- No relationships among the keys
  - No foreign keys
- No transactions across keys
  - BigTable supports transactions for a single key-value
- Simple operation-set
  - Put, Get, Delete

Here's example data in a key-value DB:

Key                | Value
-------------------|----------------------------------------------------------
product:P001       | {"name": "Wireless Mouse", "price": 24.99, "stock": 150}
product:P002       | {"name": "USB-C Cable", "price": 29.99, "stock": 200}
product:P003       | {"name": "Laptop Stand", "price": 39.99, "stock": 75}
product:P004       | {"name": "Mechanical Keyboard", "price": 89.99, "stock": 50}

- Despite its simplicity, widely used
  - BigTable was the only database at Google for many years and used by gmail, calendar, maps, and all other Google apps
  - Cloud storage services like AWS S3 and GCS are key-value stores
- If you're interested in more details, the 2006 [Bigtable paper](https://static.googleusercontent.com/media/research.google.com/en//archive/bigtable-osdi06.pdf) is a good read

## Document

> Similar to a key-value database, except that the value is a tagged document
>
> The database understands tags and queries can reference tags
>
> A document format can be XML, JSON, etc.

Here's example user data in a document DB:

```
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "username": "alice_smith",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "age": 28,
  "address": {
    "street": "123 Main St",
    "city": "Seattle",
    "state": "WA",
    "zipCode": "98101",
    "country": "USA"
  },
  "phone": "+1-206-555-0123",
  "accountType": "premium",
  "createdAt": ISODate("2024-01-15T08:30:00Z"),
  "lastLogin": ISODate("2025-10-30T09:45:00Z"),
  "preferences": {
    "notifications": true,
    "newsletter": false,
    "theme": "dark"
  },
  "tags": ["early-adopter", "active-user"]
}

{
  "_id": ObjectId("507f1f77bcf86cd799439012"),
  "username": "bob_jones",
  "email": "bob@example.com",
  "firstName": "Bob",
  "lastName": "Jones",
  "age": 35,
  "address": {
    "street": "456 Oak Ave",
    "city": "Portland",
    "state": "OR",
    "zipCode": "97201",
    "country": "USA"
  },
  "phone": "+1-503-555-0456",
  "accountType": "free",
  "createdAt": ISODate("2024-03-22T14:20:00Z"),
  "lastLogin": ISODate("2025-10-29T16:30:00Z"),
  "preferences": {
    "notifications": false,
    "newsletter": true,
    "theme": "light"
  },
  "tags": ["occasional-user"]
}

{
  "_id": ObjectId("507f1f77bcf86cd799439013"),
  "username": "carol_white",
  "email": "carol@example.com",
  "firstName": "Carol",
  "lastName": "White",
  "age": 42,
  "address": {
    "street": "789 Pine Rd",
    "city": "San Francisco",
    "state": "CA",
    "zipCode": "94102",
    "country": "USA"
  },
  "accountType": "enterprise",
  "createdAt": ISODate("2023-11-10T10:15:00Z"),
  "lastLogin": ISODate("2025-10-30T08:00:00Z"),
  "preferences": {
    "notifications": true,
    "newsletter": true,
    "theme": "dark"
  },
  "tags": ["power-user", "beta-tester"],
  "company": "Tech Corp Inc"
}
```

- **MongDB** is a popular open-source document database
  - [SQL to MongoDB mapping](https://www.mongodb.com/docs/manual/reference/sql-comparison/?utm_campaign=em_gfo_welcome_series_24_10_03&utm_source=iterable&utm_medium=email#sql-to-mongodb-mapping-chart)

## Column-oriented

> Databases that use column-centric storage

Google has tooling to convert any data set into "ColumnIO" format which allowed for fast adhoc queries without a relational DB
  - See the [Dremel paper](http://www-cs-students.stanford.edu/~adityagp/courses/cs598/papers/dremel.pdf)
  - This is the basis of Google's BigQuery cloud service

How? Let's use a simple employee table with 5 records:

| EmployeeID | Name      | Department | Salary | Age |
|------------|-----------|------------|--------|-----|
| 101        | Alice     | Engineering| 95000  | 28  |
| 102        | Bob       | Sales      | 75000  | 35  |
| 103        | Carol     | Engineering| 105000 | 42  |
| 104        | David     | Marketing  | 68000  | 29  |
| 105        | Eve       | Sales      | 82000  | 31  |

### Row-Centric Storage (Traditional Databases)

#### Conceptual Layout
Data is stored **row by row**. Each row's complete data is stored together sequentially.

```
┌─────────────────────────────────────────────────────────────┐
│ Row 1: 101, Alice, Engineering, 95000, 28                   │
├─────────────────────────────────────────────────────────────┤
│ Row 2: 102, Bob, Sales, 75000, 35                           │
├─────────────────────────────────────────────────────────────┤
│ Row 3: 103, Carol, Engineering, 105000, 42                  │
├─────────────────────────────────────────────────────────────┤
│ Row 4: 104, David, Marketing, 68000, 29                     │
├─────────────────────────────────────────────────────────────┤
│ Row 5: 105, Eve, Sales, 82000, 31                           │
└─────────────────────────────────────────────────────────────┘
```

#### Physical Disk Layout
```
Disk Blocks:
┌──────────────────────────────────────────────────────────────────┐
│ Block 1                                                          │
│ [101][Alice][Engineering][95000][28]                            │
│ [102][Bob][Sales][75000][35]                                    │
│ [103][Carol][Engineering][105000][42]                           │
├──────────────────────────────────────────────────────────────────┤
│ Block 2                                                          │
│ [104][David][Marketing][68000][29]                              │
│ [105][Eve][Sales][82000][31]                                    │
└──────────────────────────────────────────────────────────────────┘
```

#### Reading Pattern Example (no index)

**Query: SELECT Salary FROM Employees WHERE Department = 'Engineering'**

```
Read Path:
Block 1 → [101][Alice][Engineering][95000][28]  ← Read ALL, check dept, extract salary
          [102][Bob][Sales][75000][35]          ← Read ALL, skip
          [103][Carol][Engineering][105000][42] ← Read ALL, check dept, extract salary
Block 2 → [104][David][Marketing][68000][29]   ← Read ALL, skip
          [105][Eve][Sales][82000][31]          ← Read ALL, skip

Result: Need to read ALL columns even though we only need Salary and Department
```

### Column-Centric Storage (Columnar Databases)

#### Conceptual Layout
Data is stored **column by column**. All values for each column are stored together sequentially.

```
┌─────────────────────────────────────────────────────────────┐
│ EmployeeID Column: 101, 102, 103, 104, 105                  │
├─────────────────────────────────────────────────────────────┤
│ Name Column: Alice, Bob, Carol, David, Eve                  │
├─────────────────────────────────────────────────────────────┤
│ Department Column: Engineering, Sales, Engineering,         │
│                    Marketing, Sales                         │
├─────────────────────────────────────────────────────────────┤
│ Salary Column: 95000, 75000, 105000, 68000, 82000          │
├─────────────────────────────────────────────────────────────┤
│ Age Column: 28, 35, 42, 29, 31                             │
└─────────────────────────────────────────────────────────────┘
```

#### Physical Disk Layout
```
Disk Blocks:
┌──────────────────────────────────────────────────────────────────┐
│ Block 1: EmployeeID Column                                       │
│ [101][102][103][104][105]                                        │
├──────────────────────────────────────────────────────────────────┤
│ Block 2: Name Column                                             │
│ [Alice][Bob][Carol][David][Eve]                                 │
├──────────────────────────────────────────────────────────────────┤
│ Block 3: Department Column                                       │
│ [Engineering][Sales][Engineering][Marketing][Sales]              │
├──────────────────────────────────────────────────────────────────┤
│ Block 4: Salary Column                                           │
│ [95000][75000][105000][68000][82000]                            │
├──────────────────────────────────────────────────────────────────┤
│ Block 5: Age Column                                              │
│ [28][35][42][29][31]                                            │
└──────────────────────────────────────────────────────────────────┘
```

#### Reading Pattern Example

**Query: SELECT Salary FROM Employees WHERE Department = 'Engineering'**

```
Read Path:
Block 3 → [Engineering][Sales][Engineering][Marketing][Sales]
          ↑ position 1  ↑ skip  ↑ position 3  ↑ skip    ↑ skip
          
Block 4 → [95000][75000][105000][68000][82000]
          ↑ get position 1       ↑ get position 3

Result: Only read 2 blocks! (Department and Salary)
        Skip EmployeeID, Name, and Age columns entirely
```

### Side-by-Side Comparison

#### Query: SELECT AVG(Salary) FROM Employees

##### Row-Centric Storage
```
Must Read:
┌──────────────────────────────────────────────────────────┐
│ [101][Alice][Engineering][95000][28]                     │ ← Read entire row
│ [102][Bob][Sales][75000][35]                             │ ← Read entire row
│ [103][Carol][Engineering][105000][42]                    │ ← Read entire row
│ [104][David][Marketing][68000][29]                       │ ← Read entire row
│ [105][Eve][Sales][82000][31]                             │ ← Read entire row
└──────────────────────────────────────────────────────────┘

Bytes Read: ~500 bytes (ALL columns for ALL rows)
Extract: Only the Salary values, discard everything else
```

##### Column-Centric Storage
```
Must Read:
┌──────────────────────────────────────────────────────────┐
│ Salary Column: [95000][75000][105000][68000][82000]     │ ← Read ONLY Salary
└──────────────────────────────────────────────────────────┘

Bytes Read: ~25 bytes (ONLY the Salary column)
Extract: Already have exactly what we need
```

**Result: Column-centric reads ~20x less data!**

## Graph

> Stores data about relationship-rich environments using graph theory

- Motivated by social networks
- Don't relations DBs already support relationships well?
- They do, as long as you know all relationships up front
- Graph databases allow adding new relationships to existing databases
  - e.g. allow customers to "friend" one another or to "like" an agent
- The relationships are as important as the data itself

### Example

Graph databases store data using:
- **Nodes** = Things
- **Relationships** = Connections (arrows)
- **Properties** = Details (key-value pairs)

A small social network:

```
     ┌─────────────┐
     │    Alice    │
     │   age: 28   │
     └──────┬──────┘
            │
            │ FRIEND_OF
            │
            ↓
     ┌─────────────┐          ┌──────────────┐
     │     Bob     │─────────→│   TechCorp   │
     │   age: 35   │ WORKS_AT │ founded:2010 │
     └─────────────┘          └──────────────┘
```

**Three Nodes:**
1. Alice (Person)
2. Bob (Person)  
3. TechCorp (Company)

**Two Relationships:**
1. Alice → Bob (FRIEND_OF)
2. Bob → TechCorp (WORKS_AT)

A **node** represents an entity:

```
┌──────────────────────┐
│      Node: Bob       │
├──────────────────────┤
│ Label: Person        │
│                      │
│ Properties:          │
│   name: "Bob"        │
│   age: 35            │
│   email: "bob@ex.com"│
└──────────────────────┘
```

A **relationship** connects two nodes:

```
(Alice) ────────────────→ (Bob)
        FRIEND_OF
        since: 2020
```

More detailed:
```
┌─────────────────────────────────┐
│   Alice ──→ Bob                 │
├─────────────────────────────────┤
│ Type: FRIEND_OF                 │
│                                 │
│ Properties:                     │
│   since: 2020                   │
│   status: "close"               │
└─────────────────────────────────┘
```

- A query is a graph traversal
  - e.g. shortest path or degree of connectedness

- Neo4j is a popular open-source graph database
  - Short tutorial video: https://www.youtube.com/watch?v=IShRYPsmiR8

---
