#!/bin/bash

set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
-- revoke privileges on public, but keep it to install extensions

REVOKE CREATE ON SCHEMA public FROM PUBLIC;

-- create users

CREATE USER $SOCIAL_NETWORK_USER WITH ENCRYPTED PASSWORD '$SOCIAL_NETWORK_PASSWORD';

-- create all schemas

CREATE SCHEMA $SOCIAL_NETWORK_SCHEMA AUTHORIZATION $SOCIAL_NETWORK_USER;

GRANT $SOCIAL_NETWORK_USER TO $POSTGRES_USER;

-- create extensions

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA $SOCIAL_NETWORK_SCHEMA; -- provides cryptographic functions for PostgresSQL
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA $SOCIAL_NETWORK_SCHEMA; -- provides GIN index operator, used for improving similarity searches using LIKE

-- configure $SOCIAL_NETWORK_USER

GRANT ALL PRIVILEGES ON SCHEMA $SOCIAL_NETWORK_SCHEMA TO $SOCIAL_NETWORK_USER;
GRANT CONNECT ON DATABASE $POSTGRES_DB TO $SOCIAL_NETWORK_USER;
EOSQL