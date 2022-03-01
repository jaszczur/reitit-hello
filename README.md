# Reitit hello

A goal of this repo is to show how a Reitit project can be structured. It's by no means final structure, has some rough edges here and there. But IMHO it can be treated as a good starting point.

## Cosmos DB

```shell
curl -k https://localhost:8081/_explorer/emulator.pem > emulatorcert.crt
sudo keytool -alias localhost-cosmos -import -keystore $JAVA_HOME/lib/security/cacerts -file emulatorcert.crt
```

## License

MIT
