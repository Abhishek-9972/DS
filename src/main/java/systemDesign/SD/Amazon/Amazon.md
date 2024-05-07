# AMAZON

##Functional Requirements

1) Search
2) Cart/Wishlist
3) Checkout
4) View Orders
5) Recommendations

##Non Functional Requirements

1) Low Latency(Recommendations & Search)
2) High Availability
3) High Consistency(Order Placements & Payment)


## Capacity Estimation

Active Users :

300M MAU(Monthly Active Users)

Each user searches for 10 product per month

- 300 M * 10 Search per month
- 3B Search /(30 * 24 hours * 60 * 60)
- 3 * 10^9 /(30 * 10^5)
= 1k Searches/second
  
= 30 * 24 hours * 60 * 60 ~ 86400 seconds ~ 10^5 

Total Products

Assume : 1 Product requires 10MB Storage(Image + Description)
Total Product Storage : 10M * 10MB

= 10 * 10^6 * 10 * 10^6
= 100 * 10^12
= 100 TB Storage required to store all product information

