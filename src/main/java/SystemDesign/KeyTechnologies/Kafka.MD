![img.png](images/Default/img.png)

![img_1.png](images/Default/img_1.png)

![img_2.png](images/Default/img_2.png)

````
While not strictly required, the key is used to determine which partition the message is sent to. If you don't provide a key, Kafka will distribute messages across partitions using a default strategy (modern Kafka clients use a "sticky" partitioner that batches messages to the same partition for efficiency, then rotates). So when designing a large, distributed system like you're likely to be asked about in your interview, you'll want to use keys to ensure that related messages land on the same partition and are processed in order. The choice of that key is important. More on this later.
````

Different applications that independently consume the same Kafka topic should use different consumer groups. Kafka maintains offsets per consumer group, so each application can consume the same message independently and at its own pace.

![img.png](images/Default/img10.png)

# Group + topic + partition; committed offsets stored in __consumer_offsets
![img_10.png](images/Default/img_10.png)

![img_10.png](images/kafka/img_10.png)

![img.png](images/kafka/img.png)

![img_1.png](images/kafka/img_1.png)

![img_2.png](images/kafka/img_2.png)

![img_3.png](images/kafka/img_3.png)

![img_4.png](images/kafka/img_4.png)

![img_5.png](images/kafka/img_5.png)

![img_6.png](images/kafka/img_6.png)

![img_7.png](images/kafka/img_7.png)

![img_8.png](images/kafka/img_8.png)

![img_9.png](images/kafka/img_9.png)

KRaftController

![img_11.png](images/kafka/img_11.png)

![img_12.png](images/kafka/img_12.png)

# Cooperative Sticky Assignor
![img_13.png](images/kafka/img_13.png)