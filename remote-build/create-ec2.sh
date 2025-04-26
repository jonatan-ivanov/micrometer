#! /bin/bash

# Ubuntu Server 24.04 LTS (HVM)
# x86: ami-075686beab831bb7f
# arm: ami-0e8c824f386e1de06
AMI='ami-075686beab831bb7f'
INSTANCE_TYPE='t3.xlarge'
KEY_NAME='bob-the-builder'

aws ec2 run-instances \
	--image-id "$AMI" \
	--instance-type "$INSTANCE_TYPE" \
	--key-name "$KEY_NAME" \
	--user-data file://user-data.sh \
	--block-device-mappings '{"DeviceName":"/dev/sda1","Ebs":{"Encrypted":false,"DeleteOnTermination":true,"Iops":3000,"SnapshotId":"snap-02143e47f75794b95","VolumeSize":8,"VolumeType":"gp3","Throughput":125}}' \
	--network-interfaces '{"SubnetId":"subnet-08785b36a8c99ab53","AssociatePublicIpAddress":true,"DeviceIndex":0,"Groups":["sg-0f4921cbcc2dbbb50"]}' \
	--credit-specification '{"CpuCredits":"standard"}' \
	--tag-specifications '{"ResourceType":"instance","Tags":[{"Key":"Name","Value":"bob-the-builder"}]}' \
	--metadata-options '{"HttpEndpoint":"enabled","HttpPutResponseHopLimit":2,"HttpTokens":"required"}' \
	--private-dns-name-options '{"HostnameType":"ip-name","EnableResourceNameDnsARecord":false,"EnableResourceNameDnsAAAARecord":false}' \
	--count 1
