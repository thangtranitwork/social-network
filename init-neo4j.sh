#!/bin/bash
set -e

echo "Checking for dump file..."

if [ -f "/var/lib/neo4j/import/neo4j.dump" ]; then
    echo "Found dump file, importing..."

    # Import trực tiếp (Neo4j chưa start)
    neo4j-admin database load --from-path=/var/lib/neo4j/import neo4j --overwrite-destination=true

    echo "Database imported successfully!"
fi

echo "Starting Neo4j..."
# Start Neo4j normally và keep container running
exec neo4j console