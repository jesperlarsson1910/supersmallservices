#!/bin/bash
set -e

echo "==> Creating namespace..."
kubectl apply -f namespace.yaml

echo "==> Applying secrets and config..."
kubectl apply -f secrets.yaml
kubectl apply -f configmap.yaml

echo "==> Starting infrastructure..."
kubectl apply -f mysql.yaml
kubectl apply -f rabbitmq.yaml
kubectl apply -f mailhog.yaml

echo "==> Waiting for MySQL to be ready..."
kubectl wait --namespace ticketservice \
  --for=condition=ready pod \
  --selector=app=mysql \
  --timeout=120s

echo "==> Waiting for RabbitMQ to be ready..."
kubectl wait --namespace ticketservice \
  --for=condition=ready pod \
  --selector=app=rabbitmq \
  --timeout=120s

echo "==> Starting services..."
kubectl apply -f authservice.yaml
kubectl apply -f eventservice.yaml
kubectl apply -f inventoryservice.yaml

echo "==> Waiting for inventoryservice (needed by orderservice gRPC)..."
kubectl wait --namespace ticketservice \
  --for=condition=ready pod \
  --selector=app=inventoryservice \
  --timeout=120s

kubectl apply -f orderservice.yaml
kubectl apply -f notificationservice.yaml
kubectl apply -f botservice.yaml
kubectl apply -f bff.yaml

echo ""
echo "==> All resources applied. Checking status..."
kubectl get pods -n ticketservice

echo ""
echo "==> Services:"
kubectl get services -n ticketservice

echo ""
echo "==> Frontend available at http://localhost:3000 once bff pod is Ready"
