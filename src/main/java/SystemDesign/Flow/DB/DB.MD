### ID (Postgres/Cassandra)

1. snowflake(bigint) - 8 bytes - 64 bit - 64-bit, about 41 bits of timestamp, 10 bits of machine or shard ID, and 12 bits of sequence.
2. UUIDv7(uuid) - 16 bytes - 128 bit

### Index

idx (user_id, created_at DESC)

### Idempotency Key
idempotency_key UNIQUE uuid

UUID 

PK, FK, UNIQUE