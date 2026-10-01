# Redis is a single threaded, inMemory DataStructure


# InMemoryDatabase
When we say Redis is an in-memory data store, it means Redis keeps its working dataset in the server's RAM (Random Access Memory) instead of needing to read data from an SSD or HDD for every request.

![img.png](images/Redis/img.png)

![img_1.png](images/Redis/img_1.png)

![img_4.png](images/Redis/img_4.png)

# Single Threaded

![img_2.png](images/Redis/img_2.png)

![img_3.png](images/Redis/img_3.png)

The main reason redis chosed this design is to simplify concurrent access to shared data.

Values

![img_5.png](images/Redis/img_5.png)

![img_6.png](images/Redis/img_6.png)

![img_7.png](images/Redis/img_7.png)

# How does Redis write works

![img_8.png](images/Redis/img_8.png)

The key idea is: Redis Cluster has 16,384 hash slots, and every key is mapped to one slot. That slot determines which Redis primary node stores the key.

## slot=CRC16(key)mod16384


This is disabled as we want to use it just as a cache. Don't mention below two persistent models in interview.
Redis provides two main persistence options to save data to disk: RDB and AOF. 

## RDB (Real-time Data Base) Persistence Model:
   RDB is a point-in-time snapshot of the Redis dataset stored as a binary file. 


## AOF (Append-Only File) Persistence Model
AOF logs all write operations to a file in a human-readable format.

Redis Commands

String
````
SET mykey "Hello"
GET mykey
````

# Distributed Lock

# SET lock:order:101 unique-token NX EX 30

![img_9.png](images/Redis/img_9.png)

````
SET is the Redis command to create or update a key with a value.

lock:order:101 is Redis Key, nothing magical about it. We can rename it something else as well.

NX = Not eXists -> Set the key only if the key does NOT already exist.

````

![img_10.png](images/Redis/img_10.png)

![img_11.png](images/Redis/img_11.png)

![img_12.png](images/Redis/img_12.png)

## Search using latitude and longitude

![img_13.png](images/Redis/img_13.png)


![img_14.png](images/Redis/img_14.png)

## Dream 11 UseCase

![img_15.png](images/Redis/img_15.png)

For incrementing ZINCRBY contest:123 50 user101

Its a sortedSet

## RateLimiter Token Bucket Algorithm

![img_16.png](images/Redis/img_16.png)