import socket
import json

## function to Create Sample Data
def create_jrep_data():
    user_data = {
        "user_id": 12345,
        "name": "John Doe",
        "email": "john.doe@example.com",
        "age": 30
    }
    
    user_data_json = json.dumps(user_data)

    jreap_c_message = user_data_json.encode('utf-8')
    
    return jreap_c_message


## function to Send JREP Message
def send_jrep_message(host, port):
    udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    
    try:
        jreap_c_message = create_jrep_data()
        udp_socket.sendto(jreap_c_message, (host, port))
        print(f"Sent JREAP C message: {jreap_c_message}")
    except Exception as e:
        print(f"An error occurred: {e}")
    finally:
        udp_socket.close()
        print("UDP connection closed.")
        
if __name__ == "__main__":
    target_host = "127.0.0.1"
    target_port = 5555
    send_jrep_message(target_host, target_port)
