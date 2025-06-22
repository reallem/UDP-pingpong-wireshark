# UDP Ping Pong with Wireshark

This project was done as part of our university course **CS330 - Computer Networks**.   
We made a small client-server app using UDP. The client sends "ping" and the server replies with "pong".

---

## Tools Used
- Java
- Wireshark

---

## What we did:
- Client sends 10 pings.
- Server replies with pong.
- We calculate RTT (Round Trip Time).
- Then we try with packet loss (0.3, 0.5, 0.8).
- We used Wireshark to see the packets.

---

## 📸 Screenshots: Wireshark + Server Log + Ping Result

Each screenshot shows a different part of the project:
- **server-log-drop.png**  
  Server dropping "pong" messages as part of the loss simulation (positions vary per run).
- **wireshark-udp.png**  
  Captured UDP traffic between client and server using Wireshark with filters.
- **ping-comparison.png**  
  Comparison between our custom UDP Ping Pong app and the standard system ping.
(see images folder)

---

## Filters we used in Wireshark:
```text
udp
udp.port == 9999
ip.addr == 192.168.100.13
```

---

## Files in this repo:
- PingPongClient.java
- PingPongServer.java
- filters.txt
- images folder

---

## Notes:
This was a group project. We all worked on different parts.  
It helped us understand how UDP and packet analysis works.
