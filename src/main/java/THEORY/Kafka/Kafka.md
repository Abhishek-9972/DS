https://www.youtube.com/watch?v=IsgRatCefVc

![img_1.png](img_1.png) 

![img.png](img.png)

![img_2.png](img_2.png)

![img_3.png](img_3.png)

![img_4.png](img_4.png)

![img_5.png](img_5.png)

![img_6.png](img_6.png)

![img_7.png](img_7.png)

![img_8.png](img_8.png)

#Dead Letter Queue
1) Retry
2) Analysis and Debugging
3) Configuration and Monitoring

![img_9.png](img_9.png)

1) Producer produces a message via Topic T1 to Consumer
2) Message contained some error
3) Consumer will push the message to Topic T2(DLQ)
4) Consumer2 would be processing only the DLQ T2 Topic
5) Consumer2 would be pushing the message to S3
6) Lambda can be running on top of the S3 and sending out mail or alerts.

#Acknowledgments

acks = 0 - Producer will not wait for ack
acks = 1 - Producer will wait for ack that data is written to the leader partition
acks = all - Producer will with for ack that data is written to all the replica partition

#Idempotent Producers

![img_10.png](img_10.png)

![img_11.png](img_11.png)

Set one of the producer properties - enable.idempotence = true (idempotent)


