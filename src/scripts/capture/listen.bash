echo 'semicolon' | sudo tshark -i lo -f "host 127.0.0.1 and port 5555" -w captures/test.pcap
