# #!/usr/bin/env bash

# # Exit immediately if a command fails to prevent partial installations
# set -euo pipefail

# # ==========================================================
# # 1. CONFIGURATION VARIABLES
# # Change these values before running the script!
# # ==========================================================
# VM_USER="root"                           # The SSH username for your VM
# VM_IP="200.97.175.94"                    # The public IP address of your VM

# # PostgreSQL Settings
# POSTGRES_USER="bleepUser"                  # The database username your Java app will use
# POSTGRES_PASSWORD="bleepLearnhub8795346"   # The database password
# POSTGRES_DB_PROD="learnhub_db"             # The main production database
# POSTGRES_DB_STAGING="learnhub_db_staging"  # The staging database for testing
# POSTGRES_PORT="5432"                       # Custom Postgres port (matches your docker-compose)



# # Redis Settings
# REDIS_PASSWORD="bleepLearnhub8795346"       # The Redis password
# REDIS_PORT="6380"                        # Custom Redis port (matches your docker-compose)

# echo "🚀 Starting database setup on VM (${VM_IP})..."

# # ==========================================================
# # 2. CHECK SSH CONNECTIVITY
# # Ensures we can actually reach the server before starting
# # ==========================================================
# if ! ssh -q -o BatchMode=yes -o ConnectTimeout=5 "$VM_USER@$VM_IP" "echo 'SSH connected'"; then
#     echo "❌ ERROR: Cannot connect to $VM_USER@$VM_IP via SSH. Check your IP/Keys."
#     exit 1
# fi

# # ==========================================================
# # 3. REMOTE INSTALLATION & CONFIGURATION
# # Everything inside the 'EOF' block runs on your remote VM
# # ==========================================================
# ssh "$VM_USER@$VM_IP" << EOF
#     set -euo pipefail

#     echo "⚙️ Updating system packages and installing PostgreSQL & Redis..."
#     # Install the required packages without prompting for user confirmation (-y)
#     apt-get update -y && apt-get install -y postgresql postgresql-contrib redis-server

#     # ------------------------------------------------------
#     # A. POSTGRESQL CONFIGURATION
#     # ------------------------------------------------------
#     # Find the paths to the active configuration files
#     PG_CONF=\$(ls /etc/postgresql/*/main/postgresql.conf | head -n 1)
#     PG_HBA=\$(ls /etc/postgresql/*/main/pg_hba.conf | head -n 1)

#     echo "⚙️ Configuring PostgreSQL on port ${POSTGRES_PORT}..."
    
#     # 1. Allow connections from any IP. 
#     # This is REQUIRED because Docker containers (like your Spring app) appear as external network connections to the host VM.
#     sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/g" "\$PG_CONF"
#     sed -i "s/listen_addresses = 'localhost'/listen_addresses = '*'/g" "\$PG_CONF"
    
#     # 2. Change the default port from 5432 to your custom port
#     sed -i "s/^port = .*/port = ${POSTGRES_PORT}/g" "\$PG_CONF"

#     # 3. Update pg_hba.conf to allow password authentication (md5/scram-sha-256) from Docker (0.0.0.0/0)
#     if ! grep -q "0.0.0.0/0" "\$PG_HBA"; then
#         echo "host    all             all             0.0.0.0/0               scram-sha-256" >> "\$PG_HBA"
#         echo "host    all             all             0.0.0.0/0               md5" >> "\$PG_HBA"
#     fi

#     # Restart Postgres to apply the network and port changes
#     systemctl restart postgresql

#     # ------------------------------------------------------
#     # B. POSTGRESQL DATABASE & USER CREATION
#     # ------------------------------------------------------
#     echo "🗄️ Creating PostgreSQL User and Databases..."
    
#     # Run SQL commands as the default 'postgres' superuser
#     sudo -u postgres psql -p ${POSTGRES_PORT} <<PG_SQL
#         -- 1. Create the database user and set their password
#         DO \$\$
#         BEGIN
#             IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${POSTGRES_USER}') THEN
#                 CREATE USER ${POSTGRES_USER} WITH PASSWORD '${POSTGRES_PASSWORD}';
#             END IF;
#         END
#         \$\$;

#         -- 2. Create the Production Database and assign ownership
#         SELECT 'CREATE DATABASE ${POSTGRES_DB_PROD}'
#         WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${POSTGRES_DB_PROD}')\gexec
#         GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB_PROD} TO ${POSTGRES_USER};
#         ALTER DATABASE ${POSTGRES_DB_PROD} OWNER TO ${POSTGRES_USER};

#         -- 3. Create the Staging Database and assign ownership
#         SELECT 'CREATE DATABASE ${POSTGRES_DB_STAGING}'
#         WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${POSTGRES_DB_STAGING}')\gexec
#         GRANT ALL PRIVILEGES ON DATABASE ${POSTGRES_DB_STAGING} TO ${POSTGRES_USER};
#         ALTER DATABASE ${POSTGRES_DB_STAGING} OWNER TO ${POSTGRES_USER};
# PG_SQL

#     # ------------------------------------------------------
#     # C. REDIS CONFIGURATION
#     # ------------------------------------------------------
#     REDIS_CONF="/etc/redis/redis.conf"

#     echo "⚙️ Configuring Redis on port ${REDIS_PORT}..."
    
#     # 1. Change the default port from 6379 to your custom port
#     sed -i "s/^port .*/port ${REDIS_PORT}/g" "\$REDIS_CONF"
    
#     # 2. Allow connections from Docker by binding to 0.0.0.0 instead of 127.0.0.1
#     sed -i "s/^bind .*/bind 0.0.0.0/g" "\$REDIS_CONF"
    
#     # 3. Disable protected mode (required when accessing Redis from outside localhost)
#     sed -i "s/^protected-mode .*/protected-mode no/g" "\$REDIS_CONF"

#     # 4. Set the Redis Password securely
#     if grep -q "^# requirepass" "\$REDIS_CONF"; then
#         sed -i "s/^# requirepass.*/requirepass ${REDIS_PASSWORD}/g" "\$REDIS_CONF"
#     elif ! grep -q "^requirepass ${REDIS_PASSWORD}" "\$REDIS_CONF"; then
#         echo "requirepass ${REDIS_PASSWORD}" >> "\$REDIS_CONF"
#     fi

#     # Restart Redis to apply changes
#     systemctl restart redis-server

#     # ------------------------------------------------------
#     # D. VERIFICATION CHECKS
#     # ------------------------------------------------------
#     echo "------------------------------------------------------"
#     echo "🧪 Verifying PostgreSQL is listening on port ${POSTGRES_PORT}..."
#     pg_isready -p ${POSTGRES_PORT}

#     echo "🧪 Verifying Redis is listening and accepting passwords on port ${REDIS_PORT}..."
#     redis-cli -p ${REDIS_PORT} -a "${REDIS_PASSWORD}" ping
#     echo "------------------------------------------------------"
# EOF

# echo "=========================================="
# echo "✨ Setup Complete!"
# echo "Your Spring Boot app can now connect to:"
# echo "🐘 Postgres: jdbc:postgresql://host.docker.internal:${POSTGRES_PORT}/${POSTGRES_DB_PROD}"
# echo "🟥 Redis:    host.docker.internal:${REDIS_PORT}"
# echo "=========================================="