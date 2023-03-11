# Source Engine Query Cacher
![Downloads](
https://img.shields.io/github/downloads/hyperxpro/SourceEngineQueryCacher/total)

<p> High-Performance Source Engine Query Cacher for caching and responding A2S_INFO and A2S_PLAYER packets. </p>
<p> Features:
  <ol>
    <li> Built on top of Netty. </li>
    <li> Uses Direct Buffers to minimize memory copy and garbage creation in JVM. </li>
    <li> Truely Asynchronous. </li>
    <li> Uses native Epoll Transport. </li>
    <li> In-Memory Cache for storing A2S_PLAYER challenge codes. </li>
    <li> Multi-threaded with configurable thread count for maximum performance </li>
    <li> Highly configurable using Configuration file or Process arguments. </li>
  </ol>
</p>

## Requirements:
Java 11 and Linux Kernel 3.9+.

## How to run:
1. [Download](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/what-is-corretto-11.html) and Install Java 11.
2. Download Source Engine Query Cacher Binary.
3. Execute the following command: `java -jar SourceEngineQueryCacher-1.6.6.jar`
4. Source Engine Query Cacher will start and display 'Server Started on Socket: IP:PORT'.
5. Configure IPTables for routing Query Packets and everything is done.

## Redirect Query Packets to Query Cacher in Linux using IPTables
```
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF54|' -j REDIRECT --to-ports 9110
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF55|' -j REDIRECT --to-ports 9110
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF41|' -j REDIRECT --to-ports 9110
```
## Configuration
### Configuration File
Execute the following command to load configuration file: `java -jar SourceEngineQueryCacher-1.6.6.jar -c Config.conf
```
Threads: Number of Threads
StatsPPS: Enable Packets per Second Stats
StatsbPS: Enable Bits per Second Stats
GameUpdateInterval: Game Server Update rate in Milliseconds
GameUpdateSocketTimeout: Game Server Update Socket Timeout in Milliseconds
ChallengeCodeTTL: Maximum Validity of Challenge Code in Milliseconds
LocalServerIPAddress: IP Address on which Cacher Server will bind and listen
LocalServerPort: Port on which Cacher Server will bind and listen
GameServerIPAddress: Game Server IP Address
GameServerPort: Game Server Port
ReceiveBufferSize: Server Receive Buffer Size
SendBufferSize: Server Send Buffer Size
```
### Process Arguments
Example: Execute the following command to pass Process Arguments: `java -jar SourceEngineQueryCacher-1.6.6.jar -bind 192.168.1.100 -port 27015`
```
-b,--bpsStats                          Enable Bits per Second Stats
-bind <arg>                            Local Server IP Address on which Cacher Server will bind and listen
-c,--config <arg>                      Configuration File Path
-challengeCodeTTL <arg>                Maximum Validity of Challenge Code in Milliseconds
-gameip <arg>                          Game Server IP Address
-gameport <arg>                        Game Server Port
-gameUpdateRate <arg>                  Game Server Update rate in  Milliseconds
-gameUpdateTimeout <arg>               Game Server Update Socket Timeout in Milliseconds
-h,--help                              Display Usages
-p,--ppsStats                          Enable Packets per Second Stats
-port <arg>                            Local Server Port on which Cacher Server will bind and listen
-r,--receiveBuf <arg>                  Server Receive Buffer Size
-s,--sendBuf <arg>                     Server Send Buffer Size
-w,--threads <arg>                     Number of Threads
```

# Sponsors
```
FATALITY~The ImmortaLs 24x7 PuB [bl4rr0w]
Web: https://www.gametracker.com/server_info/146.56.50.40:55555/
```
