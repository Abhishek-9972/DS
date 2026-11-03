# Google Maps HLD: the full flow, explained from scratch

A beginner-friendly walkthrough of how a system like Google Maps works, with a focus on map tiles, routing and live traffic.

Think of the system as three jobs running at once:

1. **Showing the map** (tiles)
2. **Answering questions** (search, routes, ETA)
3. **Learning from drivers** (live traffic)

Behind these runs an **offline factory** that builds the map data. The doc starts with tiles, since that is the part most people have not met before.

## Part 1: What is a "tile"?

### The problem tiles solve

The whole world map is enormous. A phone can't download it, and the server can't draw a custom picture for every user on every tap. So the world is **cut into small squares** called tiles, built in advance. The phone fetches only the few squares it needs to fill the screen.

It works like a jigsaw puzzle: the app asks for the 20 or so pieces that cover your screen, and the other billions stay on the server.

### Zoom levels

Each zoom level cuts the world into a finer grid.

| Zoom (z) | Tiles in the world | What you roughly see |
| --- | --- | --- |
| 0 | 1 | the whole world |
| 1 | 4 | continents |
| 5 | 1,024 | a country |
| 10 | about 1 million | a city |
| 15 | about 1 billion | streets |
| 17-18 | many billions | buildings |

The pattern is that **zoom z has 4^z tiles**. Zooming in once splits every tile into 4 smaller, more detailed tiles.

### Tile address: z / x / y

Every tile has an address of three numbers:

- **z**: the zoom level
- **x**: the column, counted from the left
- **y**: the row, counted from the top

**Worked example:** for a spot in New Delhi (lat 28.61, lng 77.21) at zoom 12, the formula gives **tile 12/2926/1708**.

- At zoom 12 the world is a 4096 × 4096 grid of tiles.
- Longitude 77.21° falls about 71% of the way across the world, which is column 2926.
- Latitude 28.61° lands in row 1708.

Each tile at this zoom is roughly 8-10 km wide.

If you drag the map east, the app simply asks for `12/2927/1708`, the next square. Zoom in and it asks for zoom 13 tiles instead. You never download "a map". You download numbered squares.

The formulas used:

```
x = floor((lng + 180) / 360 * 2^z)
y = floor((1 - asinh(tan(lat)) / pi) / 2 * 2^z)
```

### What is inside a tile?

There are two styles:

- **Raster tile:** a pre-drawn picture (PNG). It's simple, but heavy, and the picture can't be rotated or restyled.
- **Vector tile (the modern way):** not a picture but **data**: road lines, building outlines, place names. The phone draws it on the GPU.

Vector tiles are smaller, stay sharp at any zoom, and let the app rotate or tilt the map and switch to dark mode without asking the server again. Labels can also appear in your language.

### Why tiles scale so well

Tiles are **identical for everyone**. The tile `12/2926/1708` is the same for a million people in Delhi, so it can be **cached**:

1. The app asks the **CDN** (a global network of cache servers close to users) for `v123/12/2926/1708`.
2. **Cache hit:** the CDN already has it and returns it in milliseconds. This is the case for the vast majority of requests, so almost no work reaches the origin servers.
3. **Cache miss (step 2 in the diagram):** the CDN asks **Tile Service**, which reads the tile from the **Tile Store** (cheap object storage, like a giant folder keyed by `z/x/y`). The CDN keeps a copy with a long expiry.

The first person to see a rarely viewed area pays the cost, and everyone after gets it from the cache.

### Versioning (why no purging)

Notice the `v123` in the key. When the map changes, for example a new flyover opens, the build pipeline produces tiles under `v124`. The app learns the new version and requests `v124/...` keys. Old cached tiles are simply never asked for again, so nobody has to delete cache entries across the world.

### Storage size

Summing all zoom levels up to 17 gives about 23 billion tile slots. Most are oceans, deserts and ice, which are empty or trivial, so the store only keeps what's meaningful (sparse storage).

## Part 2: The request flow, step by step

These numbers match the badges in the architecture diagram.

**Step 1: open the map.** The app knows your viewport and zoom, calculates the tile ids, and requests them from the CDN. Most requests end here.

**Step 2: cache miss.** The CDN asks Tile Service, which reads from the Tile Store. The tile is cached under its versioned key.

**Step 3: search.** You type "coffee near me", and the request goes through the **API Gateway**. The gateway handles TLS, authentication, API key and quota checks, and rate limiting before forwarding anything. **Search and Places** then does two things:

- **Autocomplete:** matches your typed prefix against a text index.
- **Geocoding:** turns an address or name into lat/lng.

The Places index is built on **geo cells** (S2-style cells), which are like tiles for search: each place belongs to a cell, so "near me" only checks nearby cells instead of every place on earth. Results are ranked by distance, popularity and relevance.

**Step 4: directions.** You pick a destination. **Routing Service** snaps your start and end points to the nearest road junctions in the road graph, then searches for the best path.

**Step 5: live traffic and ETA.** Routing asks the **Traffic Store** for current speeds, and **ETA Service** predicts travel time for each candidate route. Both are described in the next parts.

**Step 6: start navigation.** The app receives 2-3 routes, each with an ETA. When you tap Start, it opens a **long-lived WebSocket or gRPC stream** to **Navigation Service**, because turn-by-turn updates need to be pushed to you instead of polled.

## Part 3: Routing, in detail

### Roads are a graph

- **Nodes:** junctions
- **Edges:** road pieces between junctions
- **Edge cost:** travel time = length ÷ current speed (plus penalties for turns, tolls and so on)

Routing means finding the cheapest path from node A to node F.

**Example (from the diagram):** the free-flow best path is A-B-D-F = 4 + 5 + 4 = 13 minutes. A jam makes B-D take 12 minutes instead of 5, so that path becomes 20 minutes. The path A-C-E-F = 6 + 3 + 5 = 14 minutes now wins, and the route switches. This is why traffic feeds routing: it changes edge costs, not the map.

### Why it can't be plain Dijkstra

Plain Dijkstra explores outward in all directions. For a short trip that's fine. For a cross-country trip it would explore millions of local streets that are obviously irrelevant.

### Contraction hierarchies (the speed trick)

Compare how you'd plan a long drive yourself: "get to the expressway, stay on it, exit near the destination." You ignore side streets in the middle.

Contraction hierarchies do this offline: they rank roads by importance and precompute **shortcuts** over the important ones. At query time, the search climbs from the start to important roads, travels along them using shortcuts, and descends near the destination. It visits a tiny fraction of nodes, so long routes take milliseconds.

### Graph sharding

The graph for a whole continent is too big for one machine. It's split into **regional shards**, and a cross-region route is stitched together at **border nodes**. Different travel modes (drive, walk, cycle, transit) each use their own graph and cost function.

### Alternative routes

After finding the best route, the system raises the cost of its edges and searches again to find different 2nd and 3rd options.

## Part 4: Live traffic, in detail (steps 7-9)

This is the part that makes "red roads" appear.

**Step 7: pings.** While you navigate, the app sends a small **anonymous GPS ping** every few seconds through the gateway to **Location Ingest**: a rotating anonymous id, lat/lng, speed, heading and timestamp. Nothing ties it to your account.

**Step 8: Kafka.** Location Ingest validates and batches the pings and writes them to **Kafka** (a high-throughput message queue) keyed by geo cell. Kafka absorbs millions of messages per second and lets processing happen asynchronously.

**Step 9: stream processing.**

1. **Map matching:** GPS is noisy and can put you in the building next to the road. Map matching snaps each ping to the most likely road segment, using the road graph and your previous points.
2. **Windowing:** pings per segment are grouped into a sliding time window (say 1-5 minutes).
3. **Median speed:** the segment's speed is the median of those pings. A minimum ping count stops a single slow car from painting a road red.
4. **Write to the Traffic Store**, which keeps recent speeds with a short expiry.

**Example, as in the diagram:**

- Segment 101: pings are far apart, so cars are moving fast, about 62 km/h, shown green.
- Segment 102: pings are bunched up, about 9 km/h, shown red.
- Segment 103: about 28 km/h, shown orange.

Incident detection works on the same data: a sudden speed drop with many stopped pings raises an incident. History by segment, weekday and hour is also stored, which trains the ETA model and lets it predict traffic for a later departure.

## Part 5: Navigation (step 10)

**Navigation Service** tracks your progress along the route. If the ETA grows because of new congestion, or you miss a turn, it asks Routing for a new route and **pushes** it down your open stream. That's why it sits between Routing and ETA in the diagram.

## Part 6: The offline factory (step 11)

Everything above reads data that has to come from somewhere:

1. **Map Data Ingestion** collects satellite imagery, Street View, partner and government data, and user edits. ML helps extract roads, signs and buildings.
2. **Map DB** is the **source of truth**: merged, versioned and strongly consistent.
3. **Tile and Graph Builder** runs batch jobs that generate tiles for every zoom level and rebuild the routing graph, including contraction hierarchies.
4. It **publishes** to the Tile Store and Road Graph Store under a new version. The CDN needs no purge, because new versions use new keys.

## Part 7: Design ideas behind the whole system

- **Precompute what you can.** Tiles and the graph are built offline, so serving requests is cheap.
- **Static graph, live weights.** Traffic only changes edge costs, never the graph.
- **Cache at every layer.** The CDN does most of the work for tiles.
- **Partition by geography.** Tiles, graph shards, the Places index and Kafka partitions all split by location, so work stays local.
- **Strong consistency only where needed.** The Map DB must be correct, while traffic and search can lag by seconds.
- **Privacy by design.** Pings are anonymized and reduced to a speed per segment, so no per-user trail is needed.

## Sizing assumptions

These are assumptions for reasoning, not measured figures:

- 20M users navigating at the same time
- 1 GPS ping per 5 s each = 4M pings/s
- About 100 B per ping = about 400 MB/s into Kafka
- Tile slots up to zoom 17: about 23 billion, mostly empty, stored sparsely
- CDN hit rate above 95%, so Tile Service sees under 5% of tile requests
- Goal: route p99 around 1 s for a typical trip

## Caveat

This is a typical design for a system like Google Maps, and the sizing numbers are assumptions. Google's real internals aren't public in this detail.
