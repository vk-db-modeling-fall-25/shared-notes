# ERD Case Study

## Overview

- Database design is an **iterative** process
  1. Identify **business rules**
  2. Identify main **entities** and **relationships** from the business rules
  3. Develop **initial ERD**
  4. Identify **attributes** and **primary keys**
  5. Revise and review the ERD
       1. additional entities, attributes, relationships may come to light
       2. Check **normalization** of each entity
  6. Stop when end users and designers agree that the ERD fairly models the org's activities and functions
  
## Case study: ERD for a College

Identify business rules e.g. by interviewing admins

1. The college is divided into several *schools*: business, design, education, IT, etc. Each school is administered by a *dean* who's a *professor*. A professor can be a dean of at most one school and a professor is not required to serve as a dean.

    Entities and relationships:

    ```mermaid
    erDiagram
      direction LR
      PROFESSOR ||--o| SCHOOL : "is dean of"
    ```
    
    Adding keys:
    
    ```mermaid
    erDiagram
      direction LR
      PROFESSOR {
        int PROF_ID PK
      }
      SCHOOL {
        int SCHOOL_ID PK
        int DEAN_PROF_ID FK
      }
      PROFESSOR ||--o| SCHOOL : "is dean of"
    ```

2. Each school operates one or more *departments*. For example, the school of business operates an accounting department, a marketing department, etc. Each department belongs to one school.

    Entities and relationships:

    ```mermaid
    erDiagram
      direction LR
      SCHOOL ||--|{ DEPARTMENT : "operates"
    ```

    Adding keys:

    ```mermaid
    erDiagram
      direction LR
      SCHOOL {
        int SCHOOL_ID PK
        int DEAN_PROF_ID FK
      }
      DEPARTMENT {
        string DEPT_CODE PK
        int SCHOOL_ID FK
      }
      SCHOOL ||--|{ DEPARTMENT : "operates"
    ```

    Merging the ERD segments:

    ```mermaid
    erDiagram
      direction LR
      PROFESSOR {
        int PROF_ID PK
      }
      SCHOOL {
        int SCHOOL_ID PK
        int DEAN_PROF_ID FK
      }
      PROFESSOR ||--o| SCHOOL : "is dean of"
      DEPARTMENT {
        string DEPT_CODE PK
        int SCHOOL_ID FK
      }
      SCHOOL ||--|{ DEPARTMENT : "operates"
    ```

3. Each department may offer *courses*

    ```mermaid
    erDiagram
      direction LR
      PROFESSOR {
        int PROF_ID PK
      }
      SCHOOL {
        int SCHOOL_ID 
        int DEAN_PROF_ID FK
      }
      PROFESSOR ||--o| SCHOOL : "is dean of"
      DEPARTMENT {
        string DEPT_CODE PK
        int SCHOOL_ID FK
      }
      SCHOOL ||--|{ DEPARTMENT : "operates"
      COURSE {
        string CRS_CODE PK
        string DEPT_CODE FK
      }
      DEPARTMENT ||--|{ COURSE : "offer"
    ```

4. Each course may generate several *classes* (sections). Each class is in a specific *quarter*

    ```mermaid
    erDiagram
      PROFESSOR {
        int PROF_ID PK
      }
      SCHOOL {
        int SCHOOL_ID PK
        int DEAN_PROF_ID FK
      }
      PROFESSOR ||--o| SCHOOL : "is dean of"
      DEPARTMENT {
        string DEPT_CODE PK
        int SCHOOL_ID FK
      }
      SCHOOL ||--|{ DEPARTMENT : "operates"
      COURSE {
        string CRS_CODE PK
        string DEPT_CODE FK
      }
      DEPARTMENT ||--|{ COURSE : "offer"
      "CLASS" {
        string CLS_CODE PK
        string CRS_CODE FK
        string QTR_CODE FK
      }
      COURSE ||--o{ "CLASS" : generates
      QUARTER {
        string QTR_CODE PK
      }
      QUARTER ||--o{ "CLASS" : offers
    ```

5. Each department has one or more professors in it, and exactly one such professor chairs the department

    ```mermaid
    erDiagram
      PROFESSOR {
        int PROF_ID PK
        string DEPT_CODE FK
      }
      SCHOOL {
        int SCHOOL_ID PK
        int DEAN_PROF_ID FK
      }
      PROFESSOR ||--o| SCHOOL : "is dean of"
      DEPARTMENT {
        string DEPT_CODE PK
        int SCHOOL_ID FK
        int CHAIR_PROF_ID PK
      }
      SCHOOL ||--|{ DEPARTMENT : "operates"
      COURSE {
        string CRS_CODE PK
        string DEPT_CODE FK
      }
      DEPARTMENT ||--|{ COURSE : "offer"
      "CLASS" {
        string CLS_CODE PK
        string CRS_CODE FK
        string QTR_CODE FK
      }
      COURSE ||--o{ "CLASS" : generates
      QUARTER {
        string QTR_CODE PK
      }
      QUARTER ||--o{ "CLASS" : offers
      DEPARTMENT ||--|{ PROFESSOR : employs
      PROFESSOR ||--o| DEPARTMENT : chairs 
    ```

    > Constraint: a professor may not both be the dean of a school and the chair of a department in a school. Nor can a professor chair more than one department.
    >
    > Not reflected in ERD
    >
    > Typically enforced in the application or using procedural sql

6. Each professor may teach up to three classes in a quarter
   
    ```mermaid
    erDiagram
      PROFESSOR {
        int PROF_ID PK
        string DEPT_CODE FK
      }
      SCHOOL {
        int SCHOOL_ID PK
        int DEAN_PROF_ID FK
      }
      PROFESSOR ||--o| SCHOOL : "is dean of"
      DEPARTMENT {
        string DEPT_CODE PK
        int SCHOOL_ID FK
        int CHAIR_PROF_ID PK
      }
      SCHOOL ||--|{ DEPARTMENT : "operates"
      COURSE {
        string CRS_CODE PK
        string DEPT_CODE FK
      }
      DEPARTMENT ||--|{ COURSE : "offer"
      "CLASS" {
        string CLS_CODE PK
        string CRS_CODE FK
        string QTR_CODE FK
        int PROF_ID FK
      }
      COURSE ||--o{ "CLASS" : generates
      QUARTER {
        string QTR_CODE PK
      }
      QUARTER ||--o{ "CLASS" : offers
      DEPARTMENT ||--|{ PROFESSOR : employs
      PROFESSOR ||--o| DEPARTMENT : chairs
      PROFESSOR ||--o{ "CLASS" : teaches 
    ```

7. And so on....