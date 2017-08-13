To build:

```
cd ~/localwork/bitcoinj
export BRANCH=p-segwit2x
docker build -t docker.e-flamingo.net:5000/amd64/bitcoinj:$BRANCH --build-arg proxy="repository.e-flamingo.net:3142" --network ef-test --build-arg branch=$BRANCH -f docker/Dockerfile.amd64 .

```

For rpi:

```
docker build -t docker.e-flamingo.net:5000/armhf/bitcoinj:$BRANCH --build-arg proxy="192.168.1.9:3142" --build-arg branch=$BRANCH -f docker/Dockerfile.armhf .

```
