#!/usr/bin/env bash
# Cloud Run Automated Deployment Script
#MAGER™ / Construction Platform Backend
set -euo pipefail

PROJECT_ID="${GCP_PROJECT:-180667634259}"
REGION="${GCP_REGION:-europe-west1}"
SERVICE_NAME="construction-platform-api"
IMAGE_TAG="gcr.io/${PROJECT_ID}/${SERVICE_NAME}:latest"

echo "=== 1. Checking Active Google Cloud Account & Project ==="
gcloud config set project "${PROJECT_ID}"

echo "=== 2. Enabling Required Cloud Services ==="
gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  containerregistry.googleapis.com \
  cloudresourcemanager.googleapis.com

echo "=== 3. Submitting Build to Cloud Build ==="
gcloud builds submit --tag "${IMAGE_TAG}" .

echo "=== 4. Deploying Service to Cloud Run ==="
gcloud run deploy "${SERVICE_NAME}" \
  --image "${IMAGE_TAG}" \
  --platform managed \
  --region "${REGION}" \
  --allow-unauthenticated \
  --port 8080 \
  --set-env-vars "NODE_ENV=production,PORT=8080"

echo "=== 5. Fetching Production Service URL ==="
SERVICE_URL=$(gcloud run services describe "${SERVICE_NAME}" --platform managed --region "${REGION}" --format 'value(status.url)')
echo "SUCCESS! Production Backend Live URL: ${SERVICE_URL}"
echo "Health Check: curl -s ${SERVICE_URL}/api/health"
