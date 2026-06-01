#!/bin/bash
echo "==> Tearing down ticketservice namespace..."
kubectl delete namespace ticketservice
echo "==> Done. All resources removed."
