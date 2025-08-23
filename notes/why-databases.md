# Why Databases?

## Data Is Ubiquitous

- You interact with data all the time
  - From birth: birth certificate
  - To death: death certificate
  - And all the moments in between
    - shopping
    - social media
    - travel bookings
    - course registrations
    - ...  

![width:600px center](<../media/Screenshot 2025-08-01 at 10.58.06 AM.png>)


### Exercises

1. Personal data journey: list at least 10 touchpoints you had with data today

2. Pick one common activity you do (e.g. ordering food online) and list all the types of data involved in it.

(http://gosocrative.com room: DRVISH)

---

- Who maintains all this data you interact with?
  - Organizations (colleges, businesses, governments, ...)
    - store vast amounts of data
    - keep the data secure and consistent
    - retrieve useful information/insights from the stored data
- How do organizations do all this
  - Using **Databases**!

## Data Not In Databases

### 1. Paper Files

- Organized using system of folders, filing cabinets, labels, etc.
- What's the problem?
  - Well, too many to list
  - e.g. slow and cumbersome to generate reports that require aggregating across lots of files
- Thought exercise
  - 1,000 index cards, each containing a student record
  - What's the average GPA of all students from California who are CS majors?

### 2. Computer Files

- Digitize paper files into computer files

Example file containing customer orders:

<style>
table {
  width:100%;
  font-size: 0.6em
}
</style>

| Order ID | Customer ID | Customer Name | Customer Email                          | Customer City | Product ID | Product Name | Category    | Price   | Quantity | Order Date |
| -------- | ----------- | ------------- | --------------------------------------- | ------------- | ---------- | ------------ | ----------- | ------- | -------- | ---------- |
| 1001     | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 201        | Laptop       | Electronics | $999.99 | 1        | 2024-01-15 |
| 1001     | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 202        | Mouse        | Electronics | $29.99  | 2        | 2024-01-15 |
| 1002     | 502         | Jane Doe      | [jane@email.com](mailto:jane@email.com) | Los Angeles   | 203        | Keyboard     | Electronics | $79.99  | 1        | 2024-01-16 |
| 1003     | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 204        | Monitor      | Electronics | $299.99 | 1        | 2024-01-17 |

<sup><sub>(AI-generated sample data)</sub></sup>


Structural hierarchy:

- The customer orders **file**
  - Contains 4 **records** (each record describes an order line item) 
    - Each record consists of 11 **fields** (each field describes one relevant attribute about an order line item)

Main problems:

1. **Structural dependency**: access to the data in a file depends on the structure of the file (how the records are stored on disk)

   - e.g. adding a field requires changing all programs that access the file
   - Why bad?
     - Must develop a custom program for each type of query/report (as opposed to supporting ad hoc queries)
       - Long lead time for new reports
       - Discourages useful data analysis

2. **Data dependency**: access to a field depends on its data type

   - e.g. changing a field from integer to floating-point requires changing all programs that access files containing that field
   - **Logical data format** (how humans view the data) vs **Physical data format** (how computer stores the data)

   - Program must specify what to do (logical) and how to do it (physical)

3. **Data redundancy**: the same pieces of data is duplicated across records in a file and/or across files

   - e.g. customer info is repeated for each line item for each order placed by that customer
   - Why is this bad?
     1. More **storage cost** for duplicate data
     2. Poor **data integrity** (more on this soon)
     3. Poor **data security**
        - e.g. increases chance of unauthoraized access to a sensitive piece of data that has been duplicated in several files

#### Poor Data Integrity 

**Data anomalies** i.e. structural problems that occur when performing data mutations
  - **Update anomalies**
    - e.g. If John Smith changes his email, you must update multiple rows
  - **Insert anomalies**
    - e.g. Can't store customer information without an associated order
    - e.g. Need to reenter customer information for each line item
  - **Delete anomalies**
    - e.g. deleting all orders for a customer removes all customer information
    - e.g. deleting a customer requires deleting all records containing information for that customer

Data anomalies lead to **data inconsistency** 
  - Update anomaly -> some records missed in the update -> inconsistent data
  - Insert anomaly -> typo in duplicated data -> inconsistent data
  - Delete anomaly -> unintended loss of data 

### 3. Spreadsheets

- Democratized data analysis
  - Enter data in cells identified by row and column (e.g. A2)
  - Manipulate the data using functions (e.g. sum)
- When all you have is a hammer, everything looks like a nail
  - Misuse and overuse of spreadsheets
- A spreadsheet is not a database!
- Suffers from some of the same problems as file systems (e.g. data redundancy without consistency/access safeguards)

## Exercises

1. Consider the following student schedule data format:

   | STU_ID | STU_NAME | CLASS_CODE | CLASS_NAME | CLASS_UNITS | INSTR_NAME | CLASS_DAYS | CLASS_TIMES | ROOM |
   | ------ | -------- | ---------- | ---------- | ----------- | ---------- | ---------- | ----------- | ---- |
   |        |          |            |            |             |            |            |             |      |

   1. Create a spreadsheet using the above template and enter your current class schedule.
   2. Enter the class schedule of two of your classmates into the same spreadsheet.
   3. Discuss the problems (cost, redundancy, security) caused by this design.

2. Consider  the following file structure for box office data:

<style>
table {
  width:100%;
  font-size: 0.4em
}
</style>
   | Movie ID | Movie Title       | Genre  | Director        | Actor Name        | Theater Name   | Address                            | Show Date  | Tickets Sold | Revenue   |
   | -------- | ----------------- | ------ | --------------- | ----------------- | -------------- | ---------------------------------- | ---------- | ------------ | --------- |
   | 1001     | Top Gun: Maverick | Action | Joseph Kosinski | Tom Cruise        | AMC Downtown   | 123 Main St, Los Angeles, CA 90210 | 2024-07-15 | 285          | $4,417.50 |
   | 1001     | Top Gun: Maverick | Action | Joseph Kosinski | Jennifer Connelly | AMC Downtown   | 123 Main St, Los Angeles, CA 90210 | 2024-07-15 | 285          | $4,417.50 |
   | 1001     | Top Gun: Maverick | Action | Joseph Kosinski | Tom Cruise        | Cinemark Plaza | 456 Oak Ave, San Diego, CA 92101   | 2024-07-15 | 198          | $2,772.00 |
   | 1001     | Top Gun: Maverick | Action | Joseph Kosinski | Jennifer Connelly | Cinemark Plaza | 456 Oak Ave, San Diego, CA 92101   | 2024-07-15 | 198          | $2,772.00 |
   | 1002     | Avatar 2          | Sci-Fi | James Cameron   | Sam Worthington   | AMC Downtown   | 123 Main St, Los Angeles, CA 90210 | 2024-07-16 | 312          | $4,836.00 |
   | 1002     | Avatar 2          | Sci-Fi | James Cameron   | Zoe Saldana       | AMC Downtown   | 123 Main St, Los Angeles, CA 90210 | 2024-07-16 | 312          | $4,836.00 |

   <sup><sub>(AI-generated sample data)</sub></sup>
   
   1. How many records does the file contain? How many fields are there per record?
   2. What problem would you encounter if you wanted to produce a listing by city? How would you solve this problem by altering the file structure?
   4. What data redundancies do you detect? How could those redundancies lead to anomalies?

## Database Systems

- Logically related data stored in a **single logical data repository**
  - (Physical data may be stored in multiple places)
- **DBMS** = DataBase Management System
- Eliminates structural/data dependence and data redundancy issues 

![](<../media/Screenshot 2025-08-01 at 12.30.27 PM.png>)

### Functions of a DBMS:

1. **Data dictionary management**

   - Maintains **metadata** that defines the data elements and their relationships
   - Provides data abstraction and eliminates structural/data dependence

2. **Data storage management**

   - Creates and manages complex structures required for physical data storage along with performant access to the stored data

3. **Data transformation and presentation**

   - Transforms entered data to conform to required data structures
   - User doesn't have to think about the physical data format (data indpendence)
     - e.g. date entered in different formats in the US vs in England, but stored in the same way

4. **Security management**

   - Allows only **authenticated** users to perform only **authorized** operations on data

5. **Concurrency control**

   - Allows multiple users to access the database concurrently without compromising data integrity

6. **Backup and recovery management**

   - Facilitates recovery from corruptions in the live database (caused by a software bug or a hardware malfunction or a user error or whatever)

7. **Data integrity management**

   - Promotes and enforces integrity rules (defined in the data dictionary)

8. **Access language and APIs**

   - Provides data access through a query language
     - nonprocedural language (specify what not how)
     - **Structured Query Language (SQL)** de facto standard
   - Provides APIs (Application Programming Interfaces) for programs written in high-level languages to access the database

> Are you now convinced that a spreadsheet is not even close to being a database? :smile: 

### Exercises

Which DBMS function(s) does each of the following real-world scenario match?

1.	"Two bank tellers try to withdraw money from the same account at exactly the same time, potentially causing an overdraft"	
2.	"A company needs to change how they store phone numbers from (555) 123-4567 format to international +1-555-123-4567 format without breaking their applications"	
3.	"The marketing team needs customer reports but doesn't know programming; they just want to ask questions in plain English"
4. "A junior employee shouldn't see executive salaries, but HR managers need access to all employee compensation data"
5. "Power outage corrupted the student registration system during finals week - need to restore yesterday's data"	
6. "Customer enters birthday as '03/04/2024' - is that March 4th (US) or April 3rd (UK)?"
7. "Need to prevent students from registering for a class that conflicts with their schedule or exceeds credit limits"	
8. "Web developers need their Python application to retrieve and update inventory data from the company database"
9. "Amazon needs to store millions of product images and retrieve them instantly when customers browse"
10. "A hospital can't have two patients assigned the same ID number"
11. "Netflix recommendation system needs to analyze viewing patterns across millions of users"
12. "An airline's booking system crashes - need to restore all today's reservations from 1 hour ago"



