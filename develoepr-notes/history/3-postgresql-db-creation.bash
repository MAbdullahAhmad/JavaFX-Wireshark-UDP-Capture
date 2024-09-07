# Access PostgreSQL CLI (you may need to provide your password)
sudo -u postgres psql

# Inside the psql shell
CREATE DATABASE packets_db;
\c packets_db

CREATE TABLE udp_captures (
    id SERIAL PRIMARY KEY,
    start_timestamp TIMESTAMP DEFAULT NULL,
    stop_timestamp TIMESTAMP DEFAULT NULL,
    captured_data TEXT DEFAULT NULL
);

# Exit the psql shell
\q
