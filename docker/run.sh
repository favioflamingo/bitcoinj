#!/bin/bash -e

# current directory should be working directory

mvn exec:java -Dexec.mainClass=org.bitcoinj.examples.ForwardingService -Dexec.args="1dice1e6pdhLzzWQq7yMidf6j8eAg7pkY"


