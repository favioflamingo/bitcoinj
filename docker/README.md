To build:

```
cd ~/localwork/bitcoinj
export BRANCH=p-segwit2x
docker build -t docker.e-flamingo.net:5000/amd64/bitcoinj:$BRANCH --build-arg proxy="repository.e-flamingo.net:3142" --network ef-test --build-arg branch=$BRANCH -f docker/Dockerfile.amd64 .

```

For running:

```
cd /usr/src/bitcoinj
cd tools
mvn clean
mvn install
mvn exec:java -Dexec.mainClass=org.bitcoinj.tools.BuildCheckpoints -Dexec.args="-testnet"

```
