#!/bin/bash
set -e

echo "Checking import status..."

IMPORT_FLAG="/data/.import_completed"

if [ -f "$IMPORT_FLAG" ]; then
    echo "Import already completed, starting Neo4j normally..."
else
    if [ -f "/var/lib/neo4j/import/neo4j.dump" ]; then
        echo "Found dump file, importing..."

        if [ -z "$PASS" ]; then
            echo "ERROR: PASS environment variable is empty!"
            exit 1
        fi

        # Import database trước khi Neo4j start
        neo4j-admin database load --from-path=/var/lib/neo4j/import neo4j --overwrite-destination=true

        # Set initial password trước khi start
        echo "Setting initial password..."
        neo4j-admin dbms set-initial-password "$PASS"

        # Tạo flag file
        touch "$IMPORT_FLAG"
        echo "Database imported and authentication configured!"
    else
        echo "No dump file found, setting up with default auth..."
        # Vẫn cần set password cho lần đầu
        if [ ! -z "$PASS" ]; then
            neo4j-admin dbms set-initial-password "$PASS" || true
        fi
        touch "$IMPORT_FLAG"
    fi
fi

echo "Starting Neo4j console..."
exec neo4j console