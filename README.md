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
## Configuration
### Configuration File
```
Transport: Set Transport to be used (Epoll or Nio)
Threads: Number of Threads
StatsPPS: Enable Packets per Second Stats
StatsbPS: Enable Bits per Second Stats
GameUpdateInterval: Game Server Update rate in Milliseconds
GameUpdateSocketTimeout: Game Server Update Socket Timeout in Milliseconds
MaxChallengeCode: Maximum Challenge Codes to be saved
ChallengeCacheCleanerInterval: Challenge Code Cache Cleaner Interval in Milliseconds
ChallengeCodeTTL: Maximum Validity of Challenge Code in Milliseconds
ChallengeCodeCacheConcurrency: Challenge Code Cache Concurrency
IPAddress: IP Address on which Cacher Server will bind and listen
Port: Port on which Cacher Server will bind and listen
GameServerIPAddress: Game Server IP Address
GameServerPort: Game Server Port
ReceiveBufferSize: Server Receive Buffer Size
SendBufferSize: Server Send Buffer Size
FixedReceiveAllocatorBufferSize: Fixed Receive ByteBuf Allocator Buffer Size
```
### Process Arguments
```
-a,--receiveAllocatorBuf <arg>         Fixed Receive ByteBuf Allocator  Buffer Size
-b,--bpsStats                          Enable Bits per Second Stats
-bind <arg>                            IP Address on which Cacher Server will bind and listen
-c,--config <arg>                      Configuration File Path
-challengeCodeCacheCleaner <arg>       Challenge Code Cache Cleaner Interval in Milliseconds
-challengeCodeCacheConcurrency <arg>   Challenge Code Cache Concurrency
-challengeCodeTTL <arg>                Maximum Validity of Challenge Code in Milliseconds
-gameip <arg>                          Game Server IP Address
-gameport <arg>                        Game Server Port
-gameUpdateRate <arg>                  Game Server Update rate in  Milliseconds
-gameUpdateTimeout <arg>               Game Server Update Socket Timeout in Milliseconds
-h,--help                              Display Usages
-maxChallengeCode <arg>                Maximum Challenge Codes to be saved
-p,--ppsStats                          Enable Packets per Second Stats
-port <arg>                            Port on which Cacher Server will bind and listen
-r,--receiveBuf <arg>                  Server Receive Buffer Size
-s,--sendBuf <arg>                     Server Send Buffer Size
-t,--transport <arg>                   Set Transport to be used [Epoll or Nio]
-w,--threads <arg>                     Number of Threads
```
