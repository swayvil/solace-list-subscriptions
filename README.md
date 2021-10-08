# solace-list-subscriptions
List all topic subscriptions on a Solace Message-VPN

## How to
Collect the SEMP Swagger of your broker version:
(http://<BROKER_HOST>:8080/SEMP/v2/monitor/spec)[http://<BROKER_HOST>:8080/SEMP/v2/monitor/spec]

And replace the file swagger.yaml in the home folder of this repo.
### Build
```
gradle wrapper
./gradlew build
```
### Run
Either:
```
./build/staged/bin/solace-subscriptions-review http://localhost:8080 admin admin default
```
Or from fat Jar:
```
cd build/libs
java -jar solace-subscriptions-review.jar http://localhost:8080 admin admin default
```

Example of output:
```
=== Review Topic Subscriptions for Message-VPN default ===
Retrieved 2 Clients
#bridge/local/#cluster:cd17890f3f81000a614dc72f00000000/solace/306/1
#client
    #MCAST/>
    #SEMP/solace/>
    #P2P/solace/#client/>
    #P2P/solace/WnAHbdbR/#client/>
    #P2P/v:solace/#client/>
    #SEMP/v:solace/>
```