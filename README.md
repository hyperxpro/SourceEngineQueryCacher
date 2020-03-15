# SourceEngineQueryCacher
Source Engine Query Cacher

## Redirect Query Packets to Query Cacher in Linux using IPTables
```
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF54|' -j REDIRECT --to-ports 9110
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF55|' -j REDIRECT --to-ports 9110
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF41|' -j REDIRECT --to-ports 9110
```
