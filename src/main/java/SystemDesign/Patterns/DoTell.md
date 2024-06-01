---------
###SYSTEM DESIGN

---------
1) Functional Requirements
2) NonFunctional Requirements - High Availability, Scalability, Low Latency
3) Capacity Estimations - 

---------

LC6K

1) ###Load Balancer - 
   - Types - (Network Layer(L4), Application Layer(L7)) - Software Hardware -
   - Routing Algorithms : a) RoundRobin b) Weighted Round Robin c) Least Connections d) Least Response Time e) Least Bandwidth f) Hashing
   - Features - Autoscaling, Sticky Sessions, HealthChecks, Encryption, Caching, Logging, Request Redirects, Fixed Response
   - Eg : Nginx, Amazon Elastic Load Balancing
   
2) ###CAP Theorem - DB (SQL/NoSQL) - ACID/BASE - Sharding
   - C - Consistency - Consistency means all clients should see the same data at the same time no matter which node they are connected to
   - A - Availability - Availability means any client making a request for data should get a response ()
   - P - Partition Tolerance - Partition Tolerance means system continues to work despite message loss or partial failure
   
    CA - PostgreSql CP - MongoDb AP - Cassandra
   
3)  ###Caching
    - Cache Hit, Cache Miss
    - Types - WriteThoughCache, WriteAroundCache, WriteBackCache
    - Eviction Policy - FIFO, LIFO, LRU, MRU, LFU
    - eg. : Redis, Memcached, Amazon Elastic Cache
   
4) ###CDN
    - CDN is a group of geographically distributed servers that work together to provide fast delivery of internet content.Static Files(Such as Html/CSS/JS)
    - Push CDN(Small Traffic), Pull CDN(Huge Traffic)
    - CloudFront
   
5) ###CQRS - Command Query Responsibility Segregation
   - Event Sourcing 
   - ReadWrite DB
   
6) ###Circuit Breaker
   - CLOSED - Everything normal
   - OPEN - Open circuit break in case of errors beyond threshold
   - HALF OPEN - First Error occurred
    
7) ###Consistent Hashing
   - Ring, Server distributed, Hash on Key and servers, index
    
8) ###Kafka
   - Broker, Topic, Partition, Offset
    - Producer - Broker - Consumer Groups
    - ZooKeeper 

---------

Reporting, Data Team, User Team, Rate Limiter

1) ###Analytics
2) ###User Service
   ![img.png](images/User.png)
3) ###Rate Limiter
   - Leaky Bucket - FIFO
   - Token Bucket - Every refresh Token is kept in bucket
   - Fixed Window - Requests in window
   - Sliding Log - All the requests stored in time stamped log
   - Sliding Window
   
   ![img.png](images/RL.png)
4) ###Web Sockets
   ![img_1.png](images/WebSocket.png)

---------
1) ###S3, 

Object Level Storage

2) ###ElasticSearch

![img.png](images/img.png)

![img_2.png](images/img_2.png)
---------


1) ###Notifications

- SNS SES

2) ###Asset Management System

![img.png](images/Netflix.png)
---------


1) ###OrderManagementSystem

![img.png](images/Oms.png)

2) ###Inbound Services
3) ###InventoryAcquisitionSystem
4) ###Payments



###WebRTC
- PeerToPeerConnection
- If public ip is accessible connection through stun server, peer to peer conncection.
- If there is some firewall etc, we will connect via turn server
![img.png](images/webRtc.png)


###UniqueIdGenerator
![img.png](images/UniqueId.png)

Twitter Snow flake Algo

###TinyURL
![img.png](images/TinyURL.png)


###WebCrawler
![img.png](images/WebCrawler.png)

###Fanout Service

Fanout on Write : Post is pushed to all friends timeline at time of publish

Fanout on Read : OnDemand Model. Recent Posts are pulled when user loads his home page.
   - Inactive Users









