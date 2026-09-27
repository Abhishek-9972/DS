# 1. Functional Requirements
   
### Rider

````
Register/Login
Request ride
View nearby drivers
Live driver tracking
Cancel ride
Payment
Ride history
````


### Driver

````
Go online/offline
Update live location
Accept/Reject ride
Navigate
Complete ride
Earnings
````

### Admin

````
Surge pricing
Fraud detection
Analytics
````

# 2. Non Functional Requirements

```
- Highly Scalable
- Low Latency
- High Availability
- Payments should be strongly consistent
- Fault Tolerance
```

# 3. High Level Diagram

![img.png](Images/img.png)

Just one more thing from the Kafka there would be a service that would read locations and store it in Cassandra.

![img_1.png](Images/img_1.png)

````text
1. Driver logs in.
2. Driver opens WebSocket.
3. Driver sends GPS every 2–5 seconds.
4. Location Service stores latest coordinates in Redis GEO.
5. Kafka receives location events.
6. Cassandra stores historical locations.
7. Rider logs in.
8. Rider requests nearby drivers.
9. Redis returns nearest available drivers.
10. Rider requests a ride.
11. Trip Service makes call to ride matching service
11. Ride matching service searches Redis GEO.
12. Ride matching service ranks drivers.
13. Redis lock prevents double booking.
14. Trip Service pushes request through Driver's WebSocket.
15. Driver accepts.
16. Trip stored in PostgreSQL.
17. Driver continues GPS updates.
18. Rider receives live updates.
19. Trip starts.
20. Trip ends.
21. Fare calculated.
22. Payment processed.
23. Kafka broadcasts completion events.
24. Trip history becomes available to both rider and driver.
````

# Phase 1 – Driver Comes Online
## Step 1: Driver Opens the App

````
Driver launches Uber Driver app.

App authenticates using JWT/Access Token.

Request goes to API Gateway.

API Gateway verifies authentication.

User Service verifies driver account.

Driver status becomes Online.

Database:

- PostgreSQL updates:

- driver_status = ONLINE
````

## Step 2: Driver Creates WebSocket Connection

Flow:

```
Driver app performs WebSocket handshake.

API Gateway upgrades HTTP → WebSocket.

Connection reaches Location Service.

Connection remains open.

Now both sides can communicate instantly.
```

Why?
```
No repeated HTTP requests.

Low latency.

Real-time communication.
```

## Step 3: Driver Sends GPS Updates

Every 2–5 seconds the phone sends:
````
Example payload:

{
"driverId": "D123",
"latitude": 12.9716,
"longitude": 77.5946,
"timestamp": 1720000000
}
````

Location Service receives it.

## GeoHash

![img_2.png](Images/img_2.png)

![img_3.png](Images/img_3.png)

![img_4.png](Images/img_4.png)

### Boundary Search

![img_5.png](Images/img_5.png)

Internally Redis GEO commands encode coordinates into a sorted-set structure using a geospatial encoding similar to geohash.

![img_6.png](Images/img_6.png)

![img_7.png](Images/img_7.png)

![img_8.png](Images/img_8.png)

![img_9.png](Images/img_9.png)

![img_10.png](Images/img_10.png)

![img_11.png](Images/img_11.png)

![img_12.png](Images/img_12.png)

![img_13.png](Images/img_13.png)

![img_14.png](Images/img_14.png)

![img_15.png](Images/img_15.png)

![img_16.png](Images/img_16.png)

![img_17.png](Images/img_17.png)

![img_18.png](Images/img_18.png)

![img_19.png](Images/img_19.png)

The Push Gateway maintains a mapping between driverId and the driver's active WebSocket connection.

![img_20.png](Images/img_20.png)

![img_21.png](Images/img_21.png)

![img_22.png](Images/img_22.png)

![img_23.png](Images/img_23.png)