#!/bin/bash

# Function to display usage
usage() {
    echo "Usage: $0 {start|stop}"
    exit 1
}

# Check if an argument is passed
if [ -z "$1" ]; then
    usage
fi

# Start or stop the PostgreSQL server based on the argument
case "$1" in
    start)
        echo "Starting PostgreSQL server..."
        sudo service postgresql start
        ;;
    stop)
        echo "Stopping PostgreSQL server..."
        sudo service postgresql stop
        ;;
    *)
        usage
        ;;
esac
