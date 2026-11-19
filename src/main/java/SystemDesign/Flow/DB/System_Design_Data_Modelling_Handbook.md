# System Design Interview — Data Modelling Handbook

**Based on the 31 design folders in Questions.zip.** This is a whiteboard-oriented companion: it focuses on entities, keys, indexes, access patterns and storage boundaries rather than reproducing each HLD.

## How to use this in an interview

1. State the access patterns before drawing tables.
2. Identify the source of truth and separate it from cache, search index, event stream and analytics projections.
3. Draw the smallest normalized transactional model first; add denormalized read models only for demonstrated query/scaling needs.
4. For every table, explain the primary key, uniqueness/invariant, and the query it serves.
5. For Cassandra/wide-column designs, state the partition key, clustering order and partition-bounding strategy. Do not present a relational schema as a Cassandra schema.
6. Mention transactional boundaries, idempotency, retention, and how derived data is rebuilt.

**Notation:** `PK` = primary key; `FK` = foreign key; `UNIQUE` = uniqueness constraint; `JSON` = structured payload. The schemas below are logical interview schemas, not vendor-specific migration scripts. Adjust data types and constraints to the chosen database.

**Source fidelity note:** The ZIP contains a mix of detailed Markdown, brief notes and diagram-only folders. Where the source explicitly described a flow (notably Payments, Web Crawler, Live Comments, Job Scheduler, Google Maps, Calendar, Stock Trading, Uber, Amazon and Seat Booking), the model follows those concepts. Some folders contain only HLD images or very brief notes; their table designs below are proposed interview-ready models inferred from the named system and visible architecture, not claims that the source specified those exact columns.


---

## 01. URL Shortener

**Storage boundary:** PostgreSQL for durable URL ownership and metadata; Redis for hot short-code lookups; analytics may be streamed to Kafka/warehouse.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `url_mapping` | code PK, long_url, owner_id, created_at, expires_at, status | Lookup by code; unique code; optional owner listing index |
| `url_click_event` | event_id PK, code, clicked_at, referrer, country, device_type | Append-only; partition by date; aggregate asynchronously |
| `url_daily_stats` | code, stat_date, clicks, unique_visitors | PK(code, stat_date); pre-aggregated analytics |

**How to explain it:** GET /{code} → Redis GET url:{code}; on miss read url_mapping and populate cache. Create uses unique short code and idempotency if client retries.

---

## 02. Seat / Ticket Booking

**Storage boundary:** PostgreSQL is source of truth for confirmed bookings and inventory; Redis holds temporary seat locks with TTL; Kafka/outbox distributes booking events.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `event` | event_id PK, title, venue_id, starts_at, status | Index starts_at/status for discovery |
| `venue_seat` | seat_id PK, venue_id, section, row_no, seat_no | UNIQUE(venue_id, section, row_no, seat_no) |
| `show_seat_inventory` | show_id, seat_id, status, hold_id, version | PK(show_id, seat_id); conditional update/version to prevent double booking |
| `reservation` | reservation_id PK, user_id, show_id, status, expires_at, idempotency_key UNIQUE | Index(user_id, created_at); expiry worker index(status, expires_at) |
| `reservation_seat` | reservation_id, show_id, seat_id | PK(reservation_id, seat_id); UNIQUE(show_id, seat_id) for active confirmed allocation via transaction/state model |
| `payment` | payment_id PK, reservation_id, amount, currency, status, provider_ref | UNIQUE(provider_ref) where present; reservation_id index |
| `booking_outbox` | event_id PK, aggregate_id, event_type, payload, created_at, published_at | Transactional outbox |

**How to explain it:** Redis key seat-hold:{showId}:{seatId} → reservationId with TTL. Final confirmation is a durable DB transaction and must serialize against expiry. Payment retries use idempotency keys.

---

## 03. Amazon-like E-commerce

**Storage boundary:** PostgreSQL/MySQL for orders, payments and inventory transactions; search index for catalog discovery; Redis for cart/session; object storage for product media.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `customer` | customer_id PK, name, email UNIQUE, created_at | Email unique |
| `product` | product_id PK, title, description, brand_id, status | Category/status indexes |
| `product_variant` | variant_id PK, product_id FK, sku UNIQUE, attributes_json | Product variants such as size/color |
| `inventory` | sku PK, warehouse_id, available_qty, reserved_qty, version | PK(sku, warehouse_id); conditional decrement |
| `cart` | cart_id PK, customer_id, status, updated_at | Index(customer_id, status) |
| `cart_item` | cart_id, variant_id, quantity, added_at | PK(cart_id, variant_id) |
| `customer_order` | order_id PK, customer_id, status, total_amount, currency, created_at | Index(customer_id, created_at DESC) |
| `order_item` | order_item_id PK, order_id FK, variant_id, quantity, unit_price_snapshot | Preserve purchase-time price |
| `payment` | payment_id PK, order_id, amount, status, provider_ref, idempotency_key UNIQUE | Idempotent charge |
| `shipment` | shipment_id PK, order_id, carrier, tracking_no, status | Index(order_id) |

**How to explain it:** Order creation reserves inventory atomically, persists order/payment intent, and emits an outbox event. Keep price snapshots in order_item; do not recompute old orders from current catalog price.

---

## 04. Twitter / X-like Feed

**Storage boundary:** Relational store for users/tweets/follows; Redis for timelines and hot objects; Kafka for fan-out; Cassandra/wide-column store for large timeline or activity reads.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `user_account` | user_id PK, handle UNIQUE, display_name, created_at | Handle lookup |
| `follow` | follower_id, followee_id, created_at | PK(follower_id, followee_id); reverse index(followee_id, follower_id) |
| `tweet` | tweet_id PK, author_id, text, media_refs, created_at, visibility | Index(author_id, created_at DESC) |
| `tweet_engagement` | tweet_id, likes_count, repost_count, reply_count | Counters/materialized projection |
| `tweet_like` | user_id, tweet_id, created_at | PK(user_id, tweet_id); index(tweet_id) |
| `timeline_entry` | user_id, bucket, created_at, tweet_id, author_id | Cassandra PK((user_id,bucket), created_at, tweet_id) or Redis sorted set |
| `tweet_reply` | reply_id PK, parent_tweet_id, author_id, text, created_at | Index(parent_tweet_id, created_at) |

**How to explain it:** Home timeline query is by user and reverse chronology. Fan-out-on-write for ordinary accounts; fan-out-on-read or hybrid for celebrity accounts. Timeline is a derived feed, not the canonical tweet record.

---

## 05. Uber-like Ride Hailing

**Storage boundary:** PostgreSQL for trips and assignments; Redis GEO/geo-cell index for live driver locations; Kafka for trip events; time-series store for telemetry.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `rider` | rider_id PK, profile fields, created_at | Choose indexes from the primary read/write paths. |
| `driver` | driver_id PK, vehicle_id, status, rating | Index(status) |
| `driver_location` | driver_id PK, lat, lon, geohash, heading, speed, observed_at | Latest state; Redis GEO keyed by geo cell; reject stale timestamps |
| `ride_request` | request_id PK, rider_id, pickup_lat, pickup_lon, drop_lat, drop_lon, status, created_at | Index(rider_id, created_at) |
| `trip` | trip_id PK, request_id UNIQUE, rider_id, driver_id, status, fare_estimate, final_fare, started_at, ended_at | Index(driver_id/status), rider_id/time |
| `trip_location_event` | trip_id, event_time, lat, lon, sequence_no | Partition by trip/date; append-only |
| `driver_assignment` | request_id PK, driver_id, assigned_at, status, version | Atomic assignment prevents double allocation |
| `trip_payment` | payment_id PK, trip_id, amount, status, provider_ref | UNIQUE(trip_id) or attempt model |

**How to explain it:** Nearby-driver search is a geo-cell/GEO query against fresh available drivers. Persist trip assignment transactionally; Redis location is ephemeral and must not be the authority for trip ownership.

---

## 06. WhatsApp-like Messaging

**Storage boundary:** Cassandra/wide-column for partitioned message history; PostgreSQL for users and conversation membership; Redis for presence/recent state; Kafka for delivery fan-out.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `user_account` | user_id PK, phone_number UNIQUE, profile_name, created_at | Choose indexes from the primary read/write paths. |
| `conversation` | conversation_id PK, type, created_by, created_at, last_message_id | Choose indexes from the primary read/write paths. |
| `conversation_member` | conversation_id, user_id, role, joined_at, last_read_seq | PK(conversation_id,user_id); reverse lookup by user_id |
| `message` | conversation_id, bucket, message_seq, message_id, sender_id, body_or_ciphertext, sent_at, message_type | Cassandra PK((conversation_id,bucket), message_seq); clustering order ASC |
| `message_delivery` | message_id, user_id, device_id, status, delivered_at, read_at | PK(message_id,user_id,device_id) |
| `user_device` | user_id, device_id, push_token, last_seen_at | PK(user_id,device_id) |
| `user_presence` | user_id PK, state, last_active_at, expires_at | Redis TTL; online state is ephemeral |

**How to explain it:** Conversation ID is the partition key for ordered history; bucket by time or sequence to bound partition size. Sequence allocation defines ordering scope. Delivery/read receipts are idempotent state transitions.

---

## 07. Zoom-like Video Conferencing

**Storage boundary:** Relational DB for meetings and membership; Redis for ephemeral presence/session state; WebSocket for signaling; WebRTC media plane carries audio/video.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `user` | user_id PK, email UNIQUE, display_name | Choose indexes from the primary read/write paths. |
| `meeting` | meeting_id PK, host_user_id, meeting_code UNIQUE, title, scheduled_start, scheduled_end, status | Index(host_user_id, scheduled_start) |
| `meeting_participant` | meeting_id, user_id, role, joined_at, left_at, connection_state | PK(meeting_id,user_id) |
| `meeting_session` | session_id PK, meeting_id, started_at, ended_at, media_region | One meeting can have multiple sessions |
| `participant_device` | meeting_id, user_id, device_id, signaling_state, last_heartbeat | Ephemeral; Redis with TTL |
| `recording` | recording_id PK, meeting_id, object_key, status, duration, created_at | Object storage stores media; DB stores metadata |

**How to explain it:** Do not store live audio/video packets in the relational DB. Store meeting metadata durably; keep heartbeat, signaling and active participant state in Redis/session services.

---

## 08. Netflix-like Video Streaming

**Storage boundary:** PostgreSQL for catalog and entitlements; object storage for video segments/manifests; CDN for delivery; Cassandra/NoSQL for viewing history at scale; Redis for hot metadata.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `content` | content_id PK, title, description, content_type, release_date, maturity_rating | Choose indexes from the primary read/write paths. |
| `content_asset` | asset_id PK, content_id, encoding, resolution, bitrate, manifest_key, status | One asset per rendition/encoding |
| `content_episode` | episode_id PK, series_id, season_no, episode_no, content_id | UNIQUE(series_id,season_no,episode_no) |
| `user_profile` | profile_id PK, account_id, name, maturity_level | Choose indexes from the primary read/write paths. |
| `subscription` | subscription_id PK, account_id, plan_id, status, start_at, end_at | Choose indexes from the primary read/write paths. |
| `entitlement` | profile_id, content_id, entitlement_type, valid_until | Access decision |
| `playback_session` | session_id PK, profile_id, content_id, device_id, started_at, ended_at | Choose indexes from the primary read/write paths. |
| `watch_progress` | profile_id, content_id, position_ms, updated_at, completed | PK(profile_id,content_id); last-write/version policy |
| `viewing_event` | profile_id, content_id, event_time, event_type, device_id | Append-only partition by profile/time |

**How to explain it:** Video bytes live in object storage/CDN as segmented renditions; DB stores metadata and manifest/object keys. Seeking is segment/byte-range retrieval, not a DB query over video payload.

---

## 09. Facebook-like Social Network

**Storage boundary:** Relational store for identity, friendships and posts; object storage for photos/videos; Redis for feed/cache; search index for discovery; Kafka for fan-out.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `user` | user_id PK, name, email UNIQUE, created_at | Choose indexes from the primary read/write paths. |
| `friendship` | user_low_id, user_high_id, status, requested_by, updated_at | Canonical ordered pair; unique pair |
| `post` | post_id PK, author_id, text, visibility, created_at | Index(author_id,created_at DESC) |
| `post_media` | media_id PK, post_id, object_key, media_type, sort_order | Choose indexes from the primary read/write paths. |
| `comment` | comment_id PK, post_id, author_id, parent_comment_id, body, created_at | Index(post_id,created_at) |
| `reaction` | user_id, post_id, reaction_type, created_at | PK(user_id,post_id) |
| `feed_entry` | user_id, created_at, post_id, author_id | Materialized fan-out feed; partition by user/bucket |

**How to explain it:** Friendship and privacy checks are authorization boundaries. Feed entries are projections and can be rebuilt from canonical posts and relationships.

---

## 10. Airbnb-like Accommodation Booking

**Storage boundary:** PostgreSQL for listings, availability and bookings; Redis for search/cache; search engine for geo/filter search; payment provider integration.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `host` | host_id PK, user_id UNIQUE, verification_status | Choose indexes from the primary read/write paths. |
| `listing` | listing_id PK, host_id, title, location, lat, lon, capacity, status | Geo/search index |
| `listing_photo` | photo_id PK, listing_id, object_key, sort_order | Choose indexes from the primary read/write paths. |
| `availability_calendar` | listing_id, stay_date, availability_status, nightly_price, version | PK(listing_id,stay_date); date-range lookup |
| `booking` | booking_id PK, listing_id, guest_id, check_in, check_out, status, total_amount, created_at | Prevent overlapping confirmed stays; exclusion constraint or locked date rows |
| `booking_guest` | booking_id, guest_id, guest_type | Choose indexes from the primary read/write paths. |
| `payment` | payment_id PK, booking_id, amount, currency, status, provider_ref | Choose indexes from the primary read/write paths. |
| `review` | review_id PK, booking_id UNIQUE, reviewer_id, listing_id, rating, comment | Only completed stays |

**How to explain it:** Availability is date-granular. Confirm booking in a transaction that locks/claims all nights; a cached search result is not a reservation guarantee.

---

## 11. Google Docs-like Collaborative Editor

**Storage boundary:** Relational DB for documents and ACL; object/blob storage for snapshots; append-only operation log; WebSocket collaboration; OT/CRDT for concurrent edits.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `document` | document_id PK, owner_id, title, created_at, updated_at, head_version | Choose indexes from the primary read/write paths. |
| `document_acl` | document_id, principal_id, role, granted_by, created_at | PK(document_id,principal_id) |
| `document_operation` | document_id, revision, operation_id UNIQUE, actor_id, operation_payload, base_revision, created_at | PK(document_id,revision); ordered operation log |
| `document_snapshot` | snapshot_id PK, document_id, revision, object_key, created_at | Periodic compacted state |
| `document_comment` | comment_id PK, document_id, anchor_start, anchor_end, body, author_id, created_at | Anchor transformation policy required |
| `document_presence` | document_id, user_id, cursor_state, updated_at, expires_at | Redis/WebSocket ephemeral state |

**How to explain it:** Persist operations with monotonically increasing document revision; transform concurrent operations (OT) or use CRDT. Snapshot + operation log supports recovery. Presence is ephemeral.

---

## 12. LeetCode-like Online Judge

**Storage boundary:** PostgreSQL for users/problems/submissions; object storage for test cases/artifacts; queue for execution; isolated workers; analytics store for results.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `user` | user_id PK, username UNIQUE, email UNIQUE | Choose indexes from the primary read/write paths. |
| `problem` | problem_id PK, slug UNIQUE, title, difficulty, statement, time_limit_ms, memory_limit_mb | Choose indexes from the primary read/write paths. |
| `problem_test_case` | test_case_id PK, problem_id, input_object_key, expected_output_key, is_hidden, weight | Keep hidden tests access-controlled |
| `submission` | submission_id PK, user_id, problem_id, language, source_object_key, status, submitted_at | Index(user_id,submitted_at DESC); status queue |
| `judge_run` | run_id PK, submission_id, worker_id, status, started_at, finished_at | Choose indexes from the primary read/write paths. |
| `test_result` | run_id, test_case_id, verdict, runtime_ms, memory_kb | PK(run_id,test_case_id) |
| `contest` | contest_id PK, starts_at, ends_at, status | Choose indexes from the primary read/write paths. |
| `contest_participant` | contest_id, user_id, score, penalty, rank | PK(contest_id,user_id) |

**How to explain it:** Submission API persists and enqueues work; sandbox workers execute untrusted code. Store source/test artifacts in object storage; never run user code inside API processes.

---

## 13. Rate Limiter

**Storage boundary:** Redis for atomic token/leaky/sliding-window state; relational/config store for policies; optional durable usage events for billing/analytics.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `rate_limit_policy` | policy_id PK, scope_type, scope_id, algorithm, limit_value, window_ms, burst_capacity, enabled | Index(scope_type,scope_id) |
| `rate_limit_rule` | rule_id PK, policy_id, route_pattern, method, priority | Choose indexes from the primary read/write paths. |
| `rate_limit_usage` | subject_key, window_start, request_count | Redis key rl:{policy}:{subject}:{window}; TTL |
| `rate_limit_decision_log` | request_id PK, subject_key, policy_id, allowed, decided_at | Sampled/async analytics only |

**How to explain it:** Redis Lua/script or atomic operation updates counter/token state. Key by tenant/user/API key plus route as needed. Avoid a single global hot key; define fail-open vs fail-closed behavior.

---

## 14. Google Calendar

**Storage boundary:** PostgreSQL for events, attendees, ACL and reminders; Debezium/outbox to Kafka; Elasticsearch for search; Redis for calendar cache.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `calendar` | calendar_id PK, owner_user_id, name, timezone, visibility | Choose indexes from the primary read/write paths. |
| `calendar_acl` | calendar_id, principal_id, role | PK(calendar_id,principal_id) |
| `event` | event_id PK, calendar_id, organizer_id, title, description, start_at, end_at, timezone, recurrence_rule, status, version | Index(calendar_id,start_at) |
| `event_attendee` | event_id, user_id, response_status, updated_at | PK(event_id,user_id) |
| `event_instance` | instance_id PK, event_id, occurrence_start, occurrence_end, exception_status | For recurrence exceptions/overrides |
| `reminder` | reminder_id PK, event_id, user_id, fire_at, status | Index(status,fire_at) for due work |
| `event_outbox` | outbox_id PK, aggregate_id, event_type, payload, created_at, published_at | CDC/outbox to Kafka |

**How to explain it:** Write event, attendee and reminder changes in one DB transaction. Debezium/outbox emits changes; consumers are idempotent. Search must enforce calendar ACL after retrieval.

---

## 15. Yelp-like Local Search & Reviews

**Storage boundary:** Relational DB for businesses/reviews; geo/full-text search engine for discovery; Redis for hot results; object storage for photos.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `user` | user_id PK, name, email UNIQUE | Choose indexes from the primary read/write paths. |
| `business` | business_id PK, name, description, lat, lon, address, category_id, status | Geo index and text index |
| `business_hours` | business_id, day_of_week, open_time, close_time | PK(business_id,day_of_week,open_time) |
| `review` | review_id PK, business_id, user_id, rating, text, created_at | Index(business_id,created_at DESC); unique policy(user_id,business_id) if one review |
| `review_photo` | photo_id PK, review_id, object_key | Choose indexes from the primary read/write paths. |
| `business_category` | business_id, category_id | PK(business_id,category_id) |
| `business_rating_aggregate` | business_id PK, rating_sum, rating_count, average_rating, updated_at | Derived aggregate |

**How to explain it:** Search engine handles geo-distance, text relevance and filters. Reviews remain durable records in SQL; aggregate rating is a materialized projection.

---

## 16. Top-K Views / Trending Content

**Storage boundary:** Kafka for event ingestion; Flink/Spark streaming aggregation; ClickHouse for arbitrary time-range analytics; Redis sorted sets for fixed-window top-K serving.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `view_event` | event_id PK, content_id, viewer_id_hash, event_time, watch_ms, source | Append-only event stream; dedupe event_id |
| `content_metadata` | content_id PK, title, category, published_at | Choose indexes from the primary read/write paths. |
| `content_view_bucket` | content_id, window_type, window_start, view_count | Aggregate table in ClickHouse |
| `topk_snapshot` | window_type, window_start, rank, content_id, score, computed_at | PK(window_type,window_start,rank) |
| `viewer_dedupe` | event_id PK, first_seen_at | TTL/dedupe store where required |

**How to explain it:** Count events in stream processor; use event-time windows and late-event policy. Redis ZSET key topk:{window}:{start}; ClickHouse serves custom ranges. Define whether count means raw views or unique viewers.

---

## 17. Typeahead / Autocomplete

**Storage boundary:** Search index/trie/FST for prefix lookup; relational source for canonical entities; Redis for hot prefixes; Kafka/CDC for index updates.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `search_entity` | entity_id PK, entity_type, display_text, normalized_text, popularity, status | Canonical entity table |
| `prefix_suggestion` | prefix, rank, entity_id, score, generated_at | Index(prefix,rank); precomputed top suggestions |
| `query_event` | event_id PK, query_text, normalized_query, user_hash, event_time | Analytics stream; privacy/retention policy |
| `entity_synonym` | entity_id, synonym | PK(entity_id,synonym) |

**How to explain it:** Normalize input consistently; query prefix index and rank by relevance/popularity. Precompute popular prefixes and cache them. Do not scan the full entity table per keystroke.

---

## 18. Stock Broker / Trading Platform

**Storage boundary:** Relational/strongly consistent store for orders and ledger; Kafka for order/fill events; market data streaming store; immutable audit trail.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `trading_account` | account_id PK, user_id, status, cash_balance_projection, version | Balance projection only; ledger is authoritative |
| `instrument` | instrument_id PK, symbol UNIQUE, exchange, tick_size, lot_size | Choose indexes from the primary read/write paths. |
| `order` | order_id PK, account_id, instrument_id, side, order_type, quantity, limit_price, status, idempotency_key UNIQUE, created_at | Index(account_id,created_at DESC); state machine |
| `order_event` | order_id, sequence_no, event_type, payload, created_at | PK(order_id,sequence_no); immutable audit |
| `execution` | execution_id PK, order_id, exchange_execution_id UNIQUE, quantity, price, executed_at | Choose indexes from the primary read/write paths. |
| `position` | account_id, instrument_id, quantity, average_price, version | PK(account_id,instrument_id); projection |
| `ledger_entry` | entry_id PK, account_id, reference_type, reference_id, debit_credit, amount, currency, created_at | Append-only double-entry; immutable |
| `risk_check` | risk_check_id PK, order_id, check_type, result, checked_at | Choose indexes from the primary read/write paths. |

**How to explain it:** Order lifecycle is stateful and auditable. Idempotency prevents duplicate orders; fills may arrive more than once, so exchange_execution_id is unique. Ledger entries are append-only; balances/positions are projections.

---

## 19. Dropbox-like File Storage

**Storage boundary:** Object storage for file blocks; PostgreSQL for namespace/metadata and permissions; Redis for metadata cache; Kafka/outbox for sync notifications.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `user` | user_id PK, email UNIQUE | Choose indexes from the primary read/write paths. |
| `folder` | folder_id PK, owner_id, parent_folder_id, name, created_at | Index(parent_folder_id,name); uniqueness within parent |
| `file_metadata` | file_id PK, owner_id, parent_folder_id, name, current_version_id, size_bytes, mime_type, updated_at | Index(parent_folder_id,name) |
| `file_version` | version_id PK, file_id, version_no, content_hash, size_bytes, created_at | UNIQUE(file_id,version_no) |
| `file_block` | block_hash PK, object_key, size_bytes, encryption_metadata | Content-addressed deduplication |
| `version_block` | version_id, block_index, block_hash | PK(version_id,block_index) |
| `file_permission` | file_id, principal_id, permission, granted_by | PK(file_id,principal_id) |
| `sync_cursor` | user_id, device_id, last_change_seq, updated_at | PK(user_id,device_id) |

**How to explain it:** Chunk files into blocks; upload blocks directly to object storage and store hashes/manifest in metadata DB. Version manifest maps ordered block indexes to hashes. Sync uses durable change sequence/cursor.

---

## 20. Live Comments on Broadcast

**Storage boundary:** WebSocket gateways for fan-out; Kafka for ingest/buffering; Redis for recent comments/rate limiting; durable DB/object storage for replay/moderation where required.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `broadcast` | broadcast_id PK, creator_id, starts_at, status | Choose indexes from the primary read/write paths. |
| `comment` | comment_id PK, broadcast_id, user_id, sequence_no, body, created_at, moderation_status | Index(broadcast_id,sequence_no); partition by broadcast/time |
| `comment_reaction` | comment_id, user_id, reaction_type, created_at | PK(comment_id,user_id,reaction_type) |
| `comment_moderation` | moderation_id PK, comment_id, action, moderator_id, reason, created_at | Audit |
| `viewer_session` | session_id PK, broadcast_id, user_id, connected_at, last_heartbeat | Ephemeral Redis/gateway state |
| `comment_counter` | broadcast_id PK, latest_sequence, total_comments | Atomic/partitioned counter |

**How to explain it:** Broadcast audience is a fan-out problem. Persist accepted comments then publish; gateways batch/coalesce delivery and apply slow-mode/rate limits. Avoid assuming every viewer needs every comment at peak scale.

---

## 21. Auction / Bidding

**Storage boundary:** PostgreSQL for auction state and bid correctness; Redis for current-price cache; Kafka for bid/outcome events and notifications.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `auction` | auction_id PK, seller_id, item_id, starts_at, ends_at, status, reserve_price, currency, version | Index(status,ends_at) |
| `bid` | bid_id PK, auction_id, bidder_id, amount, currency, created_at, idempotency_key UNIQUE | Index(auction_id,amount DESC,created_at) |
| `auction_leader` | auction_id PK, bid_id, bidder_id, current_amount, version | Atomic conditional update / optimistic locking |
| `bid_event` | event_id PK, auction_id, bid_id, event_type, created_at | Outbox/event log |
| `auction_winner` | auction_id PK, bid_id, bidder_id, final_amount, decided_at | One winner per auction |

**How to explain it:** Bid acceptance must be serialized per auction (row lock, optimistic version/CAS or partitioned single writer). Validate auction status/time and increment/version leader atomically. Redis is a projection, not authority.

---

## 22. News Aggregator

**Storage boundary:** Relational/document store for publishers and article metadata; crawler/fetch queue; search index for full-text; object storage for extracted content/images; Redis for trending.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `publisher` | publisher_id PK, name, feed_url, domain, crawl_policy | Choose indexes from the primary read/write paths. |
| `feed` | feed_id PK, publisher_id, feed_url, last_fetched_at, etag, last_modified | Choose indexes from the primary read/write paths. |
| `article` | article_id PK, publisher_id, canonical_url UNIQUE, title, summary, content_object_key, published_at, discovered_at | Index(published_at); dedupe canonical URL |
| `article_topic` | article_id, topic_id, confidence | PK(article_id,topic_id) |
| `user_subscription` | user_id, publisher_id/topic_id, created_at | Subscription model |
| `user_read_state` | user_id, article_id, read_at, saved | PK(user_id,article_id) |
| `article_rank_snapshot` | article_id, region, score, computed_at | Trending projection |

**How to explain it:** Deduplicate by canonical URL/content fingerprint. Search index is derived. Store large extracted article bodies in object storage/document store and metadata in DB.

---

## 23. Web Crawler

**Storage boundary:** Relational/config store for crawl policy and URL metadata; distributed frontier/queue; object storage for fetched pages; search index for content.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `crawl_seed` | seed_id PK, url, priority, created_at, enabled | Choose indexes from the primary read/write paths. |
| `url_frontier` | url_hash PK, normalized_url, host, priority, next_fetch_at, status, retry_count | Partition/queue by host; unique normalized URL hash |
| `crawl_job` | job_id PK, url_hash, scheduled_at, worker_id, lease_until, status | Lease-based claiming |
| `robots_policy` | host PK, robots_content, crawl_delay_ms, fetched_at, expires_at | Cache by host |
| `fetch_result` | url_hash, fetched_at, http_status, content_hash, object_key, content_type | Partition by crawl date; object storage for body |
| `link_edge` | source_url_hash, destination_url_hash, discovered_at | PK(source_url_hash,destination_url_hash) |
| `host_crawl_state` | host PK, next_allowed_at, in_flight_count, failure_count | Enforce per-host politeness |

**How to explain it:** Normalize and dedupe URLs before enqueue. Frontier scheduling respects host crawl delay and leases. Store fetched bytes outside relational rows; keep metadata and links queryable.

---

## 24. Distributed Job Scheduler

**Storage boundary:** PostgreSQL for job definitions/state and transactional claims at moderate scale; partitioned scheduler queues/leases at scale; Kafka for run events; object storage for logs/artifacts.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `job_definition` | job_id PK, owner_id, schedule_type, cron_expression, timezone, payload_ref, enabled, created_at | Choose indexes from the primary read/write paths. |
| `job_run` | run_id PK, job_id, scheduled_for, status, attempt_no, worker_id, lease_until, started_at, finished_at | UNIQUE(job_id,scheduled_for,attempt_no); index(status,scheduled_for) |
| `job_dependency` | job_id, depends_on_job_id | PK(job_id,depends_on_job_id) |
| `worker` | worker_id PK, region, capabilities, status, last_heartbeat | Choose indexes from the primary read/write paths. |
| `job_attempt_event` | run_id, sequence_no, event_type, details, created_at | PK(run_id,sequence_no) |
| `job_schedule_cursor` | job_id PK, next_fire_at, last_enqueued_at, version | Prevent missed/duplicate schedule generation |

**How to explain it:** Separate job definition from each run. Claim due work with lease/visibility timeout and atomic state transition. At-least-once execution is typical; handlers need idempotency or dedupe.

---

## 25. Payment System

**Storage boundary:** PostgreSQL/sharded relational store for payment state, idempotency and double-entry ledger; outbox/Kafka for async workflows; provider adapter; reconciliation.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `idempotency_mapping` | idempotency_key PK, request_hash, payment_id, response_payload, created_at, expires_at | Unique idempotency gate; reject same key with different request |
| `payment` | payment_id PK, merchant_id, payer_id, amount, currency, status, provider_ref, created_at, updated_at | Index(merchant_id,created_at); unique provider reference |
| `payment_attempt` | attempt_id PK, payment_id, provider, provider_request_id, status, response_code, started_at, completed_at | Unique provider_request_id |
| `ledger_account` | account_id PK, owner_type, owner_id, currency, status | Choose indexes from the primary read/write paths. |
| `ledger_entry` | entry_id PK, payment_id, account_id, side, amount, currency, created_at | Append-only; per-payment debits/credits balance to zero |
| `refund` | refund_id PK, payment_id, amount, status, provider_ref, idempotency_key UNIQUE | Refund is a new movement, never mutate original ledger |
| `payment_outbox` | event_id PK, payment_id, event_type, payload, created_at, published_at | Transactional outbox |
| `settlement_record` | settlement_id PK, provider, provider_ref, amount, currency, settlement_date, reconciliation_status | Match provider truth to internal ledger |

**How to explain it:** A timeout is in_doubt, not automatically failed. Retry with same idempotency key; reconcile provider settlement against ledger. Saga handles visible failures; reconciliation handles unknown outcomes.

---

## 26. Ad Click Aggregator

**Storage boundary:** Kafka for click ingestion; Flink/Spark for event-time aggregation and dedupe; ClickHouse/warehouse for analytics; Redis for fast counters if needed.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `ad_campaign` | campaign_id PK, advertiser_id, budget, start_at, end_at, status | Choose indexes from the primary read/write paths. |
| `ad` | ad_id PK, campaign_id, creative_ref, target_url, status | Choose indexes from the primary read/write paths. |
| `click_event` | click_id PK, ad_id, campaign_id, user_hash, event_time, placement, country | Partition by event date; dedupe click_id |
| `impression_event` | impression_id PK, ad_id, campaign_id, user_hash, event_time | Separate impressions from clicks |
| `ad_metric_bucket` | ad_id, metric_type, bucket_start, dimension_key, metric_value | OLAP aggregate; partition by date |
| `billing_ledger` | entry_id PK, campaign_id, click_id, billable_amount, currency, created_at | Idempotent billable event reference |

**How to explain it:** Ingest click events durably, dedupe, aggregate by event time/window and dimensions. Raw events and billing-grade events should be retained separately with auditability.

---

## 27. Distributed Cache

**Storage boundary:** Cache cluster (Redis/Memcached-like) with consistent hashing/hash slots; optional metadata/control plane; durable origin remains source of truth.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `cache_key_entry` | cache_key PK, value_blob, expires_at, version, last_accessed_at | Conceptual key-value record; actual cache stores in memory |
| `cache_node` | node_id PK, address, rack, status, capacity | Control-plane membership |
| `key_placement` | hash_slot PK, primary_node_id, replica_node_ids, epoch | Routing metadata / ring or slot map |
| `cache_invalidation_event` | event_id PK, cache_key, entity_version, created_at | Outbox/stream-driven invalidation |
| `cache_stats_bucket` | node_id, bucket_start, hits, misses, evictions, bytes_used | Metrics, not hot-path OLTP |

**How to explain it:** Explain cache-aside/write-through/write-back choice, TTL, eviction, replication and rebalancing. Never treat cache as durable truth unless the product explicitly requires and configures persistence.

---

## 28. Distributed Key-Value Store

**Storage boundary:** Replicated partitioned storage engine; metadata/control plane for membership and placement; WAL/LSM persistence; consensus or quorum depending on consistency model.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `kv_record` | partition_key, item_key, value_blob, version, tombstone, updated_at | Logical schema; partition by hash(partition_key); item key within partition |
| `cluster_node` | node_id PK, address, rack, status, capacity | Membership |
| `token_range` | range_start, range_end, replica_set, epoch | PK(range_start); consistent-hash placement |
| `write_log` | log_id PK, partition_key, item_key, operation, version, created_at | WAL/replication stream |
| `hinted_handoff` | target_node_id, partition_key, item_key, version, payload, expires_at | Temporary unavailable-replica repair |
| `snapshot_manifest` | snapshot_id PK, node_id, object_key, created_at | Backup/restore metadata |

**How to explain it:** Define consistency contract first: quorum, leader-based or eventual. Partition key determines placement; version/tombstone semantics resolve updates/deletes. Replication does not replace backup/repair.

---

## 29. Google Maps-like Mapping

**Storage boundary:** Object storage/CDN for versioned map tiles; graph store for road network; geo index for places; Redis/NoSQL for live traffic; Kafka for GPS ping stream.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `map_feature` | feature_id PK, feature_type, geometry_ref, attributes, map_version | Canonical map source |
| `map_tile` | map_version, zoom, tile_x, tile_y, object_key, checksum | PK(map_version,zoom,tile_x,tile_y); sparse versioned tiles |
| `road_node` | graph_version, node_id, lat, lon, region_id | PK(graph_version,node_id) |
| `road_edge` | graph_version, edge_id, from_node_id, to_node_id, distance_m, road_class, base_speed | Index(from_node_id); directed edge |
| `road_shortcut` | graph_version, shortcut_id, from_node_id, to_node_id, unpacking_ref, base_cost | Contraction hierarchy data |
| `place` | place_id PK, name, category, lat, lon, geo_cell, address | Geo-cell index |
| `traffic_segment_speed` | road_edge_id, time_bucket, median_speed, sample_count, computed_at | PK(road_edge_id,time_bucket); TTL for live data |
| `gps_ping` | anonymous_id, event_time, lat, lon, speed, heading, geo_cell | Kafka event; short retention/anonymization |

**How to explain it:** Tiles are addressed by version/z/x/y and served via CDN. Road graph is versioned; traffic changes edge weights, not topology. GPS pings are mapped to segments and aggregated; avoid retaining identifiable trails.

---

## 30. IRCTC / Train Booking

**Storage boundary:** PostgreSQL for train schedule, quota, passenger and booking transactions; Redis for temporary holds/cache; Kafka/outbox for notifications and status updates.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `train` | train_id PK, train_no UNIQUE, train_name | Choose indexes from the primary read/write paths. |
| `train_run` | run_id PK, train_id, journey_date, status | UNIQUE(train_id,journey_date) |
| `station` | station_code PK, name, city | Choose indexes from the primary read/write paths. |
| `train_stop` | run_id, station_code, sequence_no, arrival_time, departure_time | PK(run_id,sequence_no) |
| `coach_seat` | coach_id PK, run_id, coach_no, class_type | Choose indexes from the primary read/write paths. |
| `seat_inventory` | run_id, journey_segment, class_type, seat_id, status, booking_id, version | Inventory must account for overlapping journey segments |
| `booking` | pnr PK, user_id, run_id, from_station, to_station, class_type, status, total_fare, created_at | Index(user_id,created_at) |
| `passenger` | passenger_id PK, pnr, name, age, berth_preference, status | Index(pnr) |
| `payment` | payment_id PK, pnr, amount, status, provider_ref, idempotency_key UNIQUE | Choose indexes from the primary read/write paths. |
| `waitlist_entry` | run_id, class_type, sequence_no, pnr, status | PK(run_id,class_type,sequence_no) |

**How to explain it:** A seat can be reused on non-overlapping journey segments, so inventory is not simply one seat per whole train run. Allocation must check every segment in the requested station interval atomically.

---

## 31. Flash Sale

**Storage boundary:** Relational DB for orders/payment and final inventory truth; Redis atomic counters/queues for admission and fast reservation; Kafka for asynchronous order processing.

| Entity / table | Suggested columns | Key / index / modelling note |
|---|---|---|
| `sale_event` | sale_id PK, product_id, starts_at, ends_at, status, per_user_limit | Choose indexes from the primary read/write paths. |
| `sale_inventory` | sale_id, sku, total_qty, reserved_qty, sold_qty, version | PK(sale_id,sku); conditional reservation |
| `sale_admission` | sale_id, user_id, admission_token, admitted_at, expires_at | UNIQUE(sale_id,user_id); rate/admission control |
| `flash_order` | order_id PK, sale_id, user_id, sku, quantity, status, idempotency_key UNIQUE, created_at | Index(sale_id,status) |
| `inventory_reservation` | reservation_id PK, order_id, sale_id, sku, quantity, expires_at, status | TTL/expiry worker |
| `payment` | payment_id PK, order_id, amount, status, provider_ref | Idempotent |
| `inventory_event` | event_id PK, sale_id, sku, delta, reason, created_at | Audit/reconciliation |

**How to explain it:** Use admission control/virtual waiting room to protect the backend. Redis atomic operation can throttle or reserve quickly, but durable DB confirms the order. Reconcile Redis counters against durable inventory.

---

## Cross-system interview checklist

| Concern | What to say / model |
|---|---|
| Source of truth | Identify the durable owner for each business fact. Redis, Kafka, CDN and search indexes are usually not the canonical record. |
| Idempotency | Persist an idempotency key with a unique constraint and request hash; return the prior result on safe retry. |
| Concurrency | Use conditional updates, optimistic versioning, row locks or a single-writer partition for invariants such as seat allocation, bids and inventory. |
| Outbox / CDC | Commit business state and outbox row in one transaction; publish asynchronously; consumers deduplicate. |
| Denormalization | Build query-specific projections for feeds, search, counters and timelines; make them rebuildable. |
| Cassandra | Model one table per query; partition key first, clustering columns for ordered/range reads; bucket large histories; avoid unbounded partitions and joins. |
| Redis | Define key format, TTL, eviction, atomicity and failover behavior. State what happens when cache is unavailable. |
| Kafka | Choose partition key to define ordering scope; consumers must tolerate retries/duplicates; monitor lag and DLQ. |
| Search index | Treat as derived; handle updates/deletes and enforce authorization after search. |
| Time-series/event data | Partition by time and a high-cardinality entity key; define retention, late events, dedupe and rollups. |
| Multi-region | State which data is region-local, what replicates, conflict policy, and whether writes are single-region or multi-writer. |
| Privacy/security | Model ACL/tenant boundaries; avoid logging secrets or unnecessary personal location/activity data. |

## Quick whiteboard template

For each system, draw in this order:

1. **Core entities** — nouns from the requirements.
2. **Relationships** — 1:1, 1:N, M:N and ownership.
3. **Critical invariant** — e.g. one booking per seat, one idempotent payment result, ordered messages per conversation.
4. **Primary query** — exact lookup/list/range/geo/feed/search path.
5. **Indexes / partitioning** — only those that serve those queries.
6. **Read model** — cache, search index, timeline, aggregate or materialized view.
7. **Failure recovery** — retry, dedupe, outbox, reconciliation, rebuild or compensation.

**Important:** Do not draw every table in the first 5 minutes. Start with the 4–6 core entities, then expand only when the interviewer asks about a specific access pattern or correctness boundary.
