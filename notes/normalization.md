# Normalization

## Context

- File systems suffer from structural/data dependence and data redundancy (poor data integrity, security, and storage costs)
- Is it possible to structure the data in a file system to reduce/eliminate the data redundancy problems?
- Yes!
- How? By a process called **normalization**

>  **Normalization** is a process for evaluating and correcting table structures to minimize data redundancies

- Normalization works through a sequence of stages called **normal forms**
  - Unnormalized date $\rightarrow$ First Normal Form (1NF) $\rightarrow$ Second Normal Form (2NF) $\rightarrow$ Third Normal Form (3NF) $\rightarrow$ ...



## Unnormalized Data

The following customer orders file will be used to illustrate the normalization process:

| Order ID | Customer ID | Customer Name | Customer Email                          | Customer City | All Product IDs | All Product Names | All Categories           | All Prices      | All Quantities | Order Date |
| -------- | ----------- | ------------- | --------------------------------------- | ------------- | --------------- | ----------------- | ------------------------ | --------------- | -------------- | ---------- |
| 1001     | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 201, 202        | Laptop, Mouse     | Electronics, Electronics | $999.99, $29.99 | 1, 2           | 2024-01-15 |
| 1002     | 502         | Jane Doe      | [jane@email.com](mailto:jane@email.com) | Los Angeles   | 203             | Keyboard          | Electronics              | $79.99          | 1              | 2024-01-16 |
| 1003     | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 204             | Monitor           | Electronics              | $299.99         | 1              | 2024-01-17 |

This is the same file we saw before (in [Why Databases](./why-databases.md)) except that it has repeated values in the Products, Categories, Prices, and Quantities fields for an order. The $n^{th}$ product ID in the order corresponds to the $n^{th}$ product name, category, price, and quantity.



Main problem:

- Non-atomic (multi-valued) data is some fields
  - So hard to query for one item in a multi-valued field e.g. all orders with laptops
  - Need to maintain the positional structure of the multi-valued fields in the presence of mutations

Solution:

- Convert to **first normal form (1NF)**

## Conversion to 1NF

1. **Ungroup** the multi-valued data into separate rows

    | Order ID | Customer ID | Customer Name | Customer Email                          | Customer City | Product ID | Product Name | Category    | Price   | Quantity | Order Date |
    | -------- | ----------- | ------------- | --------------------------------------- | ------------- | ---------- | ------------ | ----------- | ------- | -------- | ---------- |
    | 1001     | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 201        | Laptop       | Electronics | $999.99 | 1        | 2024-01-15 |
    | 1001     | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 202        | Mouse        | Electronics | $29.99  | 2        | 2024-01-15 |
    | 1002     | 502         | Jane Doe      | [jane@email.com](mailto:jane@email.com) | Los Angeles   | 203        | Keyboard     | Electronics | $79.99  | 1        | 2024-01-16 |
    | 1003     | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 204        | Monitor      | Electronics | $299.99 | 1        | 2024-01-17 |

    - Yes, this seems to have increased the redundancy, but that will be remedied after further normalization stages

2. Identify a **primary key (PK)**

    - What's a PK?

      > A field/attribute (or an irreducible set of fields/attributes) that unique identifies each row/record

    - What's a PK in the above table?
      - (Order ID, Product ID)
      - It identifies a unique order line item

| Normal Form | Characteristic                               |
| ----------- | -------------------------------------------- |
| 1NF         | Table format, no repeating groups, PK chosen |

Main problem:

- Too much needless data redundancy 
  - e.g. Customer info

Solution:

- Convert to higher normal forms
  - This requires identifying dependencies between the fields/attributes and using that information to split a table into multiple related tables with minimal controlled redundancy

## Functional Dependence

- What is functional dependence?

  > The field/attribute B is functionally dependent on the field/attribute A if each value of A determines one and only one

  - e.g. Product Name is functionally dependent on Product ID
    - Notation: **Product ID -> Product Name**
      - Product ID is the **determinant** field/attribute
      - Product Name is the **dependent** field/attribute
  - What are some other functional dependencies in the example table?
  - Note that a PK is a determinant of all other fields/attributes
    - (Order ID, Product ID) -> (Customer ID, Customer Name, Customer Email, Customer City, Product Name, Category, Price, Quantity, Order Date)

- **Full functional dependence**

  - A functional dependence in which all the fields/attributes in the determinant are neceassry to identify the dependent
  - Which attributes have full functional dependence on the PK in the example?
    - (Order ID, Product ID) -> (Price, Quantity)

- **Partial dependency**

  > A functional dependency in which the determinant is only part of the PK

  - This can only happen if the PK is composite i.e. made up of more than one field/attribute
  - Question to ask: "Does this field need all key fields to uniquely identify it?"
  - What are some partial dependencies in the example table?
    - Product ID -> (Product Name, Category)
    - Order ID -> (Customer ID, Customer Name, Customer Email, Customer City, Order Date)

- **Transitive dependency**
  - A -> B, where both A and B are non-key fields/attributes
    - Detail: non-key means not a PK nor any other candidate PK (we'll learn later about candidate keys)
  - Transitive because PK -> A and A -> B
  - Transitive dependencies in the above table?
    - Customer ID -> (Customer Name, Custimer Email, Customer City)

## Conversion to 2NF

1. Make new tables to eliminate *partial dependencies*
   1. For each PK subset that is a determinant in a partial dependency, make a new table with that subset as PK
      - e.g. two new tables with PKs Product ID and Order ID respectively
   2. Retain that subset in the original table, to establish a relationship with the new table
   3. Move the dependent fields to the new table



**Orders**:

| Order ID (PK) | Customer ID | Customer Name | Customer Email                          | Customer City | Order Date |
| ------------- | ----------- | ------------- | --------------------------------------- | ------------- | ---------- |
| 1001          | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 2024-01-15 |
| 1001          | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 2024-01-15 |
| 1002          | 502         | Jane Doe      | [jane@email.com](mailto:jane@email.com) | Los Angeles   | 2024-01-16 |
| 1003          | 501         | John Smith    | [john@email.com](mailto:john@email.com) | New York      | 2024-01-17 |



**Products**:

| Product ID (PK) | Product Name | Category    |
| --------------- | ------------ | ----------- |
| 201             | Laptop       | Electronics |
| 202             | Mouse        | Electronics |
| 203             | Keyboard     | Electronics |
| 204             | Monitor      | Electronics |



**Order Line Items**:

| Order ID (PK) | Product ID (PK) | Price   | Quantity |
| ------------- | --------------- | ------- | -------- |
| 1001          | 201             | $999.99 | 1        |
| 1001          | 202             | $29.99  | 2        |
| 1002          | 203             | $79.99  | 1        |
| 1003          | 204             | $299.99 | 1        |

- What has 2NF achieved?
  - Full Product info duplication has been reduced to only Product ID duplication
    - Product Name and Product Category are no longer duplicated anywhere
    - Product ID in the Order Line Items relates each record to a record in the Products table
  - But the new Orders table has not helped much (yet)
    - Orders still has redundant customer info due to the transitive dependency with customer ID as determinant

| Normal Form | Characteristic                               |
| ----------- | -------------------------------------------- |
| 1NF         | Table format, no repeating groups, PK chosen |
| 2NF         | 1NF and no partial dependencies              |

> [!NOTE]
>
> Note that a 1NF table is automatically in 2NF if the PK has a single field/attribute (because that means there are no partial dependencies )

## Conversion to 3NF

Make new tables to eliminate *transitive dependencies*

1. For each non-key subset that is a determinant in a transitive dependency, make a new table with that subset as PK
   - e.g. one new table with PK Customer ID
2. Retain that subset in the original table, to establish a relationship with the new table
3. Move the dependent fields to the new table



**Customers**:

| Customer ID (PK) | Customer Name | Customer Email                          | Customer City |
| ---------------- | ------------- | --------------------------------------- | ------------- |
| 501              | John Smith    | [john@email.com](mailto:john@email.com) | New York      |
| 502              | Jane Doe      | [jane@email.com](mailto:jane@email.com) | Los Angeles   |



**Orders**:

| Order ID (PK) | Customer ID | Order Date |
| ------------- | ----------- | ---------- |
| 1001          | 501         | 2024-01-15 |
| 1001          | 501         | 2024-01-15 |
| 1002          | 502         | 2024-01-16 |
| 1003          | 501         | 2024-01-17 |



**Products**:

| Product ID (PK) | Product Name | Category    |
| --------------- | ------------ | ----------- |
| 201             | Laptop       | Electronics |
| 202             | Mouse        | Electronics |
| 203             | Keyboard     | Electronics |
| 204             | Monitor      | Electronics |



**Order Line Items**:

| Order ID (PK) | Product ID (PK) | Price   | Quantity |
| ------------- | --------------- | ------- | -------- |
| 1001          | 201             | $999.99 | 1        |
| 1001          | 202             | $29.99  | 2        |
| 1002          | 203             | $79.99  | 1        |
| 1003          | 204             | $299.99 | 1        |

- What has 2NF achieved?
  - Full customer info duplication has been reduced to only Customer ID duplication
    - Customer Name/Email/City are no longer duplicated anywhere
    - Customer ID in the Orders table relates each Orders record to a record in the Customers table



| Normal Form | Characteristic                               |
| ----------- | -------------------------------------------- |
| 1NF         | Table format, no repeating groups, PK chosen |
| 2NF         | 1NF and no partial dependencies              |
| 3NF         | 2NF and no transitive dependencies           |

## Recap

1. Unnormalized table Customer Orders with multi-valued fields

2. 1NF tables (unrolling muti-valued fields and identifying PK)

   1. Customer Orders: (Order ID, Product ID) -> (Customer ID, Customer Name, Customer Email, Customer City, Product Name, Category, Price, Quantity, Order Date)

3. 2NF tables (eliminating partial dependencies)

   1. Orders: Order ID -> (Customer ID, Customer Name, Customer Email, Customer City, Order Date)

   2. Products: Product ID -> (Product Name, Category)

   3. Order Line Items: (Order ID, Product ID) -> (Price, Quantity)

      - Order ID relates it to Orders

      - Product ID relates it to Products

4. 3 NF tables (eliminating transitive dependencies)

   1. Customers: Customer ID -> (Customer Name, Customer Email, Customer City)
   2. Orders: Order ID -> (Customer ID, Order Date)
   3. Products: Product ID -> (Product Name, Category)
   4. Order Line Items: (Order ID, Product ID) -> (Price, Quantity)



What did this achieve?

1. Removed Customer info duplication in each line item of an order in Customer Orders 
   1. Single record for each customer
2. Reduced Product info duplication in all line items for the same product across orders in Customer Orders to only duplicating the Product ID
   1. Single record for each product
3. Teased apart the *entities* in the data
   1. Customers, Orders, Products, Order Line Items



## Higher Normal Forms?

- From a structural perspective, higher the normal form, the better
- But
  - Stop at 3NF for most puproses
  - Why?
    - Normalization reduces performance because now data has to be fetched from related tables instead of from a single table
      - e.g. to generate an invoice for an order ID you have to read the following
        - Customer ID from Orders
          - Then, customer details from Customers
        - Line item details from Order Line Items
          - Then, product details from Products
    - In fact, sometimes you deliberately **denormalize** data to improve performance for queries of interest