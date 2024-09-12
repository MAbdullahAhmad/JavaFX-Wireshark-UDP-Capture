#!/bin/bash

echo -n '{"data":{"source":"udp","from":"eth0","message":"Hello Developer","time":"today"}}' | nc -u 127.0.0.1 5005
