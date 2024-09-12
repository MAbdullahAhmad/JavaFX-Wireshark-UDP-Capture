#!/usr/local/bin/python3.12

import socket

# Set up the UDP server
UDP_IP = "127.0.0.1"  # Loopback interface (lo)
UDP_PORT = 5005       # Port to listen on

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)  # UDP
sock.bind((UDP_IP, UDP_PORT))

print(f"Listening on {UDP_IP}:{UDP_PORT}...")

while True:
    data, addr = sock.recvfrom(1024)  # Buffer size of 1024 bytes
    print(f"Received message: {data} from {addr}")

    # Respond with "Start", "Stop", or some information
    if data == b'start':
        sock.sendto(b'Start signal received', addr)
    elif data == b'stop':
        sock.sendto(b'Stop signal received', addr)
    else:
        sock.sendto(b'Information received: ' + data, addr)
