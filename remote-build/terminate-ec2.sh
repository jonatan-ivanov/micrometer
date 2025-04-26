#! /bin/bash

INSTANCE_IDS=$(aws ec2 describe-instances --filters 'Name=tag:Name,Values=bob-the-builder' --query "Reservations[].Instances[].InstanceId" | jq -r '. | join(" ")')
echo "Terminating instances: $INSTANCE_IDS"
aws ec2 terminate-instances --instance-ids "$INSTANCE_IDS"
