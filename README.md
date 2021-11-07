# Source Engine Query Cacher

## This is a hard fork of
https://github.com/hyperxpro/SourceEngineQueryCacher
And as such breaks compatibility with pre existing tests and configs. Please make sure you take the updated configs from this repository.

<p> High-Performance Source Engine Query Cacher for caching and responding to A2S_INFO, A2S_RULES and A2S_PLAYER packets.</p>
<p> Features:
  <ol>
    <li> Built on top of Netty. </li>
    <li> Uses Direct Buffers to minimize memory copy and garbage creation in JVM. </li>
    <li> Truely Asynchronous. </li>
    <li> Uses native Epoll Transport. </li>
    <li> Hardens the server against DDOS attempts by decoupling A2S responses from the main game loop and CPU core </li>
    <li> Hardens the server against DDOS attempts by quickly dropping malformed packets. </li>
    <li> Hardens the server against attempts of abusing it as a reflection surface in steam amplification attacks. </li>
    <li> Now also with proper challenge code processing for A2S_INFO, A2S_RULES and A2S_PLAYER packet types </li>
    <li> In-Memory Cache for storing A2S_CHALLENGE codes. </li>
    <li> Multi-threaded with configurable thread count for maximum performance </li>
    <li> Highly configurable using Configuration file or Process arguments. </li>
  </ol>
</p>

## Requirements:
Java 11 and Linux Kernel 3.9+.

## How to run:
1. [Download](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/what-is-corretto-11.html) and Install Java 11.
2. Download Source Engine Query Cacher Binary.
3. Execute the following command: `java -jar SourceEngineQueryCacher-1.7.0.jar`
4. Source Engine Query Cacher will start and display 'Server Started on Socket: IP:PORT'.
5. Configure IPTables for routing Query Packets and everything is done.

## Redirect Query Packets to Query Cacher in Linux using IPTables
```
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF54|' -j REDIRECT --to-ports 9110
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF55|' -j REDIRECT --to-ports 9110
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF56|' -j REDIRECT --to-ports 9110
iptables -t nat -A PREROUTING -p udp --dport 27015 --match string --algo kmp --hex-string '|FFFFFFFF41|' -j REDIRECT --to-ports 9110
```
## Configuration
### Configuration File
Execute the following command to load configuration file: `java -jar SourceEngineQueryCacher-1.7.0.jar -c Config.conf
```
Threads: Number of Threads
StatsPPS: Enable Packets per Second Stats
StatsbPS: Enable Bits per Second Stats
GameUpdateRate: Game Server Info Update retrieval interval in Milliseconds
GameUpdateTimeout: Game Server Info Update Socket Timeout in Milliseconds
ChallengeCodeTTL: Maximum Validity of Challenge Code in Milliseconds
LocalServerIPAddress: IP Address on which Cacher Server will bind and listen
LocalServerPort: Port on which Cacher Server will bind and listen
GameServerIPAddress: Game Server IP Address
GameServerPort: Game Server Port
ReceiveBufferSize: Server Receive Buffer Size
SendBufferSize: Server Send Buffer Size
```
### Process Arguments
Example: Execute the following command to pass Process Arguments: `java -jar SourceEngineQueryCacher-1.7.0.jar -bind 192.168.1.100 -port 27015`
```
-b,--bpsStats                          Enable Bits per Second Stats
-bind <arg>                            Local Server IP Address on which Cacher Server will bind and listen
-c,--config <arg>                      Configuration File Path
-challengeCodeTTL <arg>                Maximum Validity of Challenge Code in Milliseconds
-gameip <arg>                          Game Server IP Address
-gameport <arg>                        Game Server Port
-gameUpdateRate <arg>                  Game Server Info Update retrieval interval in Milliseconds
-gameUpdateTimeout <arg>               Game Server Info Update Socket Timeout in Milliseconds
-h,--help                              Display Usages
-p,--ppsStats                          Enable Packets per Second Stats
-port <arg>                            Local Server Port on which Cacher Server will bind and listen
-r,--receiveBuf <arg>                  Server Receive Buffer Size
-s,--sendBuf <arg>                     Server Send Buffer Size
-w,--threads <arg>                     Number of Threads
```
