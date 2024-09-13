#!/bin/bash

# Find the latest .pcap file in the captures directory
latest_pcap=$(ls -t captures/*.pcap | head -n 1)

echo latest $latest_pcap

# Check if a .pcap file exists
if [ -z "$latest_pcap" ]; then
  echo "No pcap file found in captures directory."
  exit 1
fi

# Run tshark to read the latest .pcap file and extract the hex data
sudo tshark -r "$latest_pcap" -Y "udp" -T fields -e data | while read -r line; do
  # Parse the hex data to ASCII using echo and xxd
  echo "$line" | xxd -r -p
  echo
done