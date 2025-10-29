# Big Data

## Overview

> Big Data refers to data that displays the characteristics of volume, velocity, and variety (**the 3 Vs**) to an extent that makes the data unsuitable for management by a relational database management system.

These characteristics can be defined as follows:
- **Volume**: the quantity of data to be stored
- **Velocity**: the speed at which data is entering the system
- **Variety**: the variations in the structure of the data to be stored

![alt text](<../media/Screenshot 2025-10-29 at 2.26.01 PM.png>)

- Pioneers of Big Data tech
  - Google (to index the web)
    - e.g. Google File System (GFS)
    - e.g. MapReduce (distributed data processing)
    - e.g. BigTable (key-value store)
  - Amazon (for web commerce at scale)
    - e.g. Dynamo (key-value store)
  - Facebook (for social graph processing)
    - e.g. Cassandra

Today, tech advancement has increased the opportunity for organizations to generate and track data (e.g. via personal connected devices) and Big Data has come to include scenarios in which not all of the 3 Vs are present.

## Volume

Units of data volume:

| Amount of Data | Name     | Abbreviation |
| -------------- | -------- | ------------ |
| 1024 bytes     | kibibyte | KiB          |
| 1024 KiB       | mebibyte | MiB          |
| 1024 MiB       | gibibyte | GiB          |
| 1024 GiB       | tebibyte | TiB          |
| 1024 TiB       | pebibyte | PiB          |
| 1024 PiB       | exbibyte | EiB          |
| 1024 EiB       | zebibyte | ZiB          |

- kilo-, mega-, giga- etc. increase by 1000x
- kibi-, mibi-, gibi- etc. increase by 1024x ($1024 = 2^{10}$)
- The largest storage systems today (e.g. cloud storage at Amazon, Google, Microsoft) are approaching a Zebibyte

- How to handle a large volume of data?
    - **Scale Up** (aka **Vertical Scaling**)
      - Increase the CPU, RAM, Disk of each storage machine
      - Keep the number of machines fixed
    - **Scale Out** (aka **Horizontal Scaling**)
      - Keep the CPU, RAM, Disk of each machine fixed
      - Increase the number of machines

![alt text](<../media/Screenshot 2025-10-29 at 2.32.15 PM.png>)

|           | Capacity        | Cost             | Coordination    |
| --------- | --------------- | ---------------- | --------------- |
| Scale Up  | 👎 machine limit | 👎 specialized hw | 👍 few machines  |
| Scale Out | 👍 add machines  | 👍 commodity hw   | 👎 many machines |

- RDBMS requires high coordination (tables related via common attributes)
  - So can only scale up
    - So lower capacity and higher cost
      - So unsuitable for Volume
- NoSQL compromises on relational power (e.g. limited txn support) and in return can scale out

## Velocity

- Example: a cloud storage system like GCS handles ~10 million requests per second
- If a scaled up machine can handle ~10K requests a second, would need 1000 machines!
- So **requires scaling out** (same as to handle volume)
- So RDBMS not a good fit 

## Variety

- Structured data: data that conforms to a predefined model (e.g. a table schema)
  - RDBMS requires this!
- Unstructured data: can be anything, does not conform to a model
  - e.g. videos, texts, emails, etc.
  - Semi-structured data: parts are structured and parts are unstructured
- Real world is full of unstructured data
- NoSQL
  - Ingest unstructured data
  - Impose structure as needed for applications, during retrieval and processing

---