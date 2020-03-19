# Source Engine Query Cacher
Source Engine Query Cacher for caching and responding A2S_INFO and A2S_PLAYER packets.


## How to run:
1. Download and Install Java 11.
2. Download Source Engine Query Cacher Binary.
3. Execute the following command: SourceEngineQueryCacher-1.0.0.0.jar
4. Source Engine Query Cacher will start and display 'Server Started on Socket: IP:PORT'.
5. Configure IPTables for routing Query Packets and everything is done.

## Redirect Query Packets to Query Cacher in Linux using IPTables
```
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF54|' -j REDIRECT --to-ports 9110
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF55|' -j REDIRECT --to-ports 9110
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF41|' -j REDIRECT --to-ports 9110
```
