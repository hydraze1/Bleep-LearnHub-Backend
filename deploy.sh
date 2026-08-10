#!/usr/bin/env bash

# Exit immediately if a command fails, treat unset variables as an error, and catch pipeline failures
set -euo pipefail

# ==========================================================
# 0. TARGET ENVIRONMENT SELECTION
# Usage: ./deploy.sh <staging|prod>
# ==========================================================
ENV_TYPE="${1:-}"

if [ "$ENV_TYPE" != "staging" ] && [ "$ENV_TYPE" != "prod" ]; then
    echo "❌ ERROR: Target environment must be specified explicitly!"
    echo "   Usage: $0 <staging|prod>"
    echo "   Example: $0 staging"
    echo "   Example: $0 prod"
    exit 1
fi

ENV_FILE=".env.${ENV_TYPE}"

if [ -f "$ENV_FILE" ]; then
    echo "📋 Loading deployment configuration from ${ENV_FILE}..."
    set -a
    source "$ENV_FILE"
    set +a
else
    echo "❌ ERROR: Configuration file ${ENV_FILE} not found!"
    exit 1
fi

# ==========================================================
# 1. DEPLOYMENT TOGGLE CHECK
# ==========================================================
if [ "${AUTO_DEPLOY:-false}" != "true" ]; then
    echo "⏸️  AUTO_DEPLOY is set to 'false' in ${ENV_FILE}."
    echo "   Deployment to [${ENV_TYPE}] is currently DISABLED. Exiting safely."
    exit 0
fi

# ==========================================================
# 2. SERVER CONFIGURATION
# ==========================================================
VM_USER="${VM_USER:-root}"
VM_IP="${VM_IP:-200.97.175.94}"
REMOTE_DIR="${REMOTE_DIR:-/opt/bleep/bleep-learnhub-backend}"
ZIP_NAME="bleep-backend-${ENV_TYPE}-deploy.zip"

IMAGE_NAME="${IMAGE_NAME:-bleep-learnhub-backend}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
TAR_GZ_FILE="${IMAGE_NAME}-${IMAGE_TAG}.tar.gz"
COMPOSE_FILE="docker-compose.yml"

echo "🚀 Starting Safe Deployment for Bleep LearnHub Backend [${ENV_TYPE}]..."

# ==========================================================
# 3. PRE-FLIGHT LOCAL CHECKS
# ==========================================================
if [ ! -f "$TAR_GZ_FILE" ] || [ ! -f "$COMPOSE_FILE" ]; then
    echo "❌ ERROR: Missing deployment files! Ensure $TAR_GZ_FILE and $COMPOSE_FILE exist."
    echo "   Run './build.sh ${ENV_TYPE}' first."
    exit 1
fi

echo "✅ Local files verified for [${ENV_TYPE}]."

# ==========================================================
# 4. PRE-FLIGHT REMOTE CHECKS
# ==========================================================
echo "🔍 Checking SSH connection to $VM_IP..."
if ! ssh -o BatchMode=yes -o ConnectTimeout=5 "$VM_USER@$VM_IP" "echo 'SSH successful'"; then
    echo "❌ ERROR: Cannot connect to VM ($VM_IP). Check your VPN, IP, or SSH keys."
    exit 1
fi

echo "🔍 Ensuring remote directory exists ($REMOTE_DIR)..."
ssh "$VM_USER@$VM_IP" "mkdir -p $REMOTE_DIR"

# ==========================================================
# 5. PACKAGE & TRANSFER
# ==========================================================
echo "📦 Packaging deployment files (including ${ENV_FILE} as .env)..."
rm -f "$ZIP_NAME"
# Create temporary .env copy for packaging
cp "$ENV_FILE" .env
zip "$ZIP_NAME" "$TAR_GZ_FILE" "$COMPOSE_FILE" .env
rm -f .env

echo "🚚 Transferring $ZIP_NAME to VM..."
scp "$ZIP_NAME" "$VM_USER@$VM_IP:~/"

# ==========================================================
# 6. REMOTE EXECUTION (THE SAFE DEPLOYMENT ROUTINE)
# ==========================================================
echo "⚙️  Executing secure deployment on remote VM..."

ssh "$VM_USER@$VM_IP" << EOF
    # Stop execution if anything fails on the remote side
    set -e

    echo "--> [1/7] Unzipping new files into $REMOTE_DIR..."
    unzip -o ~/$ZIP_NAME -d $REMOTE_DIR
    rm -f ~/$ZIP_NAME

    cd $REMOTE_DIR

    echo "--> [2/7] Gracefully stopping old containers (Isolated to this project)..."
    # This safely stops only the containers defined in this specific docker-compose.yml
    if docker compose -p "${COMPOSE_PROJECT_NAME}" ps -q | grep -q .; then
        docker compose -p "${COMPOSE_PROJECT_NAME}" down
    else
        echo "    No existing running containers found for this project. Moving on."
    fi

    echo "--> [3/7] Capturing current old image ID for cleanup..."
    # We grab the ID of the old image before we load the new one so we can safely delete it later
    OLD_IMAGE_ID=\$(docker images -q ${IMAGE_NAME}:${IMAGE_TAG} 2>/dev/null || echo "")

    echo "--> [4/7] Uncompressing and loading new Docker image..."
    gunzip -f -k $TAR_GZ_FILE
    docker load -i ${IMAGE_NAME}-${IMAGE_TAG}.tar
    
    # Clean up the uncompressed tar immediately to save disk space
    rm -f ${IMAGE_NAME}-${IMAGE_TAG}.tar

    echo "--> [5/7] Starting new containers..."
    docker compose -p "${COMPOSE_PROJECT_NAME}" up -d

    echo "--> [6/7] Cleaning up deployment archives..."
    rm -f $TAR_GZ_FILE

    echo "--> [7/7] Cleaning up old unused image..."
    NEW_IMAGE_ID=\$(docker images -q ${IMAGE_NAME}:${IMAGE_TAG} 2>/dev/null || echo "")
    
    # If an old image existed, and its ID is different from the newly loaded one, delete it
    if [ -n "\$OLD_IMAGE_ID" ] && [ "\$OLD_IMAGE_ID" != "\$NEW_IMAGE_ID" ]; then
        echo "    Deleting old replaced image: \$OLD_IMAGE_ID"
        docker rmi "\$OLD_IMAGE_ID" 2>/dev/null || echo "    Could not remove old image (might still be referenced)."
    else
        echo "    No old orphaned images to delete."
    fi
EOF

# ==========================================================
# 7. LOCAL CLEANUP
# ==========================================================
rm -f "$ZIP_NAME" "$TAR_GZ_FILE" "${IMAGE_NAME}-${IMAGE_TAG}.tar"

echo "------------------------------------------------------"
echo "✨ Deployment to [${ENV_TYPE}] successfully completed with zero downtime overlap!"
echo "------------------------------------------------------"





# #!/usr/bin/env bash

# # Exit immediately if a command fails, treat unset variables as an error, and catch pipeline failures
# set -euo pipefail

# # ==========================================================
# # 1. SERVER CONFIGURATION
# # ==========================================================
# VM_USER="root"                           # SSH username
# VM_IP="200.97.175.94"                    # VM IP address
# REMOTE_DIR="/opt/bleep/bleep-learnhub-backend" 
# ZIP_NAME="bleep-backend-deploy.zip"

# IMAGE_NAME="bleep-learnhub-backend"
# IMAGE_TAG="latest"
# TAR_GZ_FILE="${IMAGE_NAME}-${IMAGE_TAG}.tar.gz"
# COMPOSE_FILE="docker-compose.yml"

# echo "🚀 Starting Safe Deployment for Bleep LearnHub Backend..."

# # ==========================================================
# # 2. PRE-FLIGHT LOCAL CHECKS
# # ==========================================================
# if [ ! -f "$TAR_GZ_FILE" ] || [ ! -f "$COMPOSE_FILE" ]; then
#     echo "❌ ERROR: Missing deployment files! Ensure $TAR_GZ_FILE and $COMPOSE_FILE exist."
#     echo "   Run ./build.sh first."
#     exit 1
# fi

# echo "✅ Local files verified."

# # ==========================================================
# # 3. PRE-FLIGHT REMOTE CHECKS
# # ==========================================================
# echo "🔍 Checking SSH connection to $VM_IP..."
# if ! ssh -o BatchMode=yes -o ConnectTimeout=5 "$VM_USER@$VM_IP" "echo 'SSH successful'"; then
#     echo "❌ ERROR: Cannot connect to VM. Check your VPN, IP, or SSH keys."
#     exit 1
# fi

# echo "🔍 Ensuring remote directory exists ($REMOTE_DIR)..."
# ssh "$VM_USER@$VM_IP" "mkdir -p $REMOTE_DIR"

# # ==========================================================
# # 4. PACKAGE & TRANSFER
# # ==========================================================
# echo "📦 Packaging deployment files..."
# rm -f "$ZIP_NAME"
# zip "$ZIP_NAME" "$TAR_GZ_FILE" "$COMPOSE_FILE"

# echo "🚚 Transferring $ZIP_NAME to VM..."
# scp "$ZIP_NAME" "$VM_USER@$VM_IP:~/"

# # ==========================================================
# # 5. REMOTE EXECUTION (THE SAFE DEPLOYMENT ROUTINE)
# # ==========================================================
# echo "⚙️  Executing secure deployment on remote VM..."

# ssh "$VM_USER@$VM_IP" << EOF
#     # Stop execution if anything fails on the remote side
#     set -e

#     echo "--> [1/7] Unzipping new files..."
#     unzip -o ~/$ZIP_NAME -d $REMOTE_DIR
#     rm -f ~/$ZIP_NAME

#     cd $REMOTE_DIR

#     echo "--> [2/7] Gracefully stopping old containers (Isolated to this project)..."
#     # This safely stops only the containers defined in this specific docker-compose.yml
#     if docker compose ls | grep -q "$REMOTE_DIR"; then
#         docker compose down
#     else
#         echo "    No existing running containers found for this project. Moving on."
#     fi

#     echo "--> [3/7] Capturing current old image ID for cleanup..."
#     # We grab the ID of the old image before we load the new one so we can safely delete it later
#     OLD_IMAGE_ID=\$(docker images -q ${IMAGE_NAME}:${IMAGE_TAG} 2>/dev/null || echo "")

#     echo "--> [4/7] Uncompressing and loading new Docker image..."
#     gunzip -f -k $TAR_GZ_FILE
#     docker load -i ${IMAGE_NAME}-${IMAGE_TAG}.tar
    
#     # Clean up the uncompressed tar immediately to save disk space
#     rm -f ${IMAGE_NAME}-${IMAGE_TAG}.tar

#     echo "--> [5/7] Starting new containers..."
#     docker compose up -d

#     echo "--> [6/7] Cleaning up deployment archives..."
#     rm -f $TAR_GZ_FILE

#     echo "--> [7/7] Cleaning up old unused image..."
#     NEW_IMAGE_ID=\$(docker images -q ${IMAGE_NAME}:${IMAGE_TAG} 2>/dev/null || echo "")
    
#     # If an old image existed, and its ID is different from the newly loaded one, delete it
#     if [ -n "\$OLD_IMAGE_ID" ] && [ "\$OLD_IMAGE_ID" != "\$NEW_IMAGE_ID" ]; then
#         echo "    Deleting old replaced image: \$OLD_IMAGE_ID"
#         docker rmi "\$OLD_IMAGE_ID" 2>/dev/null || echo "    Could not remove old image (might still be referenced)."
#     else
#         echo "    No old orphaned images to delete."
#     fi
# EOF

# # ==========================================================
# # 6. LOCAL CLEANUP
# # ==========================================================
# rm -f "$ZIP_NAME" "$TAR_GZ_FILE" "${IMAGE_NAME}-${IMAGE_TAG}.tar"

# echo "------------------------------------------------------"
# echo "✨ Deployment successfully completed with zero downtime overlap!"
# echo "------------------------------------------------------"
