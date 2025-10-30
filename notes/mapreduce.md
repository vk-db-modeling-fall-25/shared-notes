# MapReduce

## Overview

- We can store Big Data in a distributed FS and handle the volume and velocity
- What about analyzing the data and handling its variety (unstructured data)?
- Brute force: full scan
  - This is what I did to search for a particular request's log entry in AWS S3 to debug issues
    - Scan ran overnight!
- Full scans can be sped up by using multiple processes, each scanning a data shard (e.g. a file block on a data node)
  - This can be used to solve problems like searching for a particular piece of data or filtering the data (in minutes rather than hours/days)
  - But need infrastructure to set up the multiple processes and coordinate the data sharding
- What about joins on the scanned data?
  - Examples 
    - Total number of requests over a day for each customer
    - Total units sold for each product listed on each invoice
  - Can implement a distributed program to do this for each problem
    - But complicated, time-consuming, and error-prone
- Analogy
  - To extract information from a filesystem you have to write a custom program for each use case
  - DBMS solves this by providing infrastructure that supports different use cases with minimal effort

> **MapReduce** provides a general-purpose infrastructure and programming framework to process large data sets with minimal effort

## Case Study: Word Count

- In a networking class project I ask my students to build a distributed coordinator-volunteers system to count the word-frequencies in Shakespeare's plays
  - [Doc](https://lwtech-my.sharepoint.com/:w:/g/personal/vishesh_khemani_lwtech_edu/EQjfY_pfQbRBjhORPzPOZAgB1OM5qHj3f87ukzpQBbo2Fg?e=0QoI0a)
- That is quite a bit of work to implement
- Can MapReduce help?
- Yes!

![alt text](../media/mapreduce.excalidraw.png)

- All you have to do is code the Map function and the Reduce function
- The MapReduce infrastructure takes care of the rest! 

## MapReduce Playground

- I've coded up a mapreduce framework (in Java) that runs on a single machine
- It obviously doesn't manifest the distributed-processing power of real-world mapreduce
- But you can use it to play around with mapreduce without getting bogged down in setting up infrastructure
  - If you're interested in the full infra, take a look at [Hadoop MR](https://hadoop.apache.org/docs/r1.2.1/mapred_tutorial.html)

Here's my [code](/java/src/examples/wordcount/Main.java) for the word-count problem (compare with the networking project code for this):
```java
public class Main {

    public static void main(String[] args) {
        Mapper<String, Integer> mapper = (String line, Consumer<Entry<String, Integer>> consumer) -> {
            for (String word : line.split(" ")) {
                word = word.toLowerCase();
                consumer.accept(Map.entry(word, 1));
            }
        };
        Reducer<String, Integer> reducer = (String key, Collection<Integer> values, Consumer<String> consumer) -> {
            int sum = 0;
            for (int v : values) {
                sum += v;
            }
            consumer.accept(key + ": " + sum);
        };
        Spec<String, Integer> spec = new Spec<>("java/src/examples/data",
                mapper, reducer, "java/src/examples/wordcount");
        Runner<String, Integer> runner = new Runner<>(spec);
        runner.run();
    }
}
```

## Use Cases

- One offs
  - Grep across request logs for a large service (~millions of qps)
  - ...
- Full Systems
  - Generate Google Map from geo data
    - Input: features (e.g. road, building, restaurant)
    - Output: Vector drawing instructions for a map tile at a particular zoom and at a particular location
      - Zoom 0: one tile for the world
      - Zoom 1: divide into 4 tiles
      - And so on
  - Generate billing for SaaS
    - Input: request logs
    - Output: per-customer bill based on requests
  - ...

---