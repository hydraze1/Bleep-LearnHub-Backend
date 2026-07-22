#!/usr/bin/env bash

# Exit immediately if any command fails
set -e

# ==========================================
# Configuration
# ==========================================
IMAGE_NAME="bleep-learnhub-backend"
IMAGE_TAG="latest"
FULL_IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"

TAR_FILE="${IMAGE_NAME}-${IMAGE_TAG}.tar"
GZ_FILE="${TAR_FILE}.gz"

echo "🚀 Starting local build for ${FULL_IMAGE}..."

# 1. Clean up old archives
echo "🧹 Cleaning up previous build archives..."
rm -f "${TAR_FILE}" "${GZ_FILE}"

# 2. Build Docker Image
echo "📦 Building Docker image..."
docker build -t "${FULL_IMAGE}" .

# 3. Export Docker Image to .tar
echo "💾 Exporting image to ${TAR_FILE}..."
docker save -o "${TAR_FILE}" "${FULL_IMAGE}"

# 4. Compress to .tar.gz
echo "🗜️  Compressing into ${GZ_FILE}..."
gzip -f -k "${TAR_FILE}"

echo "=========================================="
echo "✅ Build & Package Complete!"
echo " Image:   ${FULL_IMAGE}"
echo " Archive: ./${GZ_FILE}"
echo "=========================================="