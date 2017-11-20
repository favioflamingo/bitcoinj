/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitcoinj.examples;

import org.bitcoinj.core.listeners.NewBestBlockListener;
import org.bitcoinj.core.*;
import org.bitcoinj.store.*;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.net.BlockingClientManager;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Build Checkpoints
 * 
 * mvn exec:java -Dexec.mainClass=org.bitcoinj.examples.BuildCheckpoints
 */
public class BuildCheckpoints {
    private static Address forwardingAddress;
    private static WalletAppKit kit;
    
    private static NetworkParameters params = MainNetParams.get();

    public static void main(String[] args) throws Exception {
        // This line makes the log output more compact and easily read, especially when using the JDK log adapter.
        BriefLogFormatter.init();
       /* if (args.length < 1) {
            System.err.println("Usage: address-to-send-back-to [regtest|testnet]");
            return;
        }*/
        
        
	//System.setProperty("socksProxyHost", "127.0.0.1");
	//System.setProperty("socksProxyPort", "9050");


        // Figure out which network we should connect to. Each one gets its own set of files.
        
        //NetworkParameters params = RegTestParams.get();

        String filePrefix = "checkpoint-service-mainnet";

        // Parse the address given as the first parameter.
        //forwardingAddress = Address.fromBase58(params, "mzCZzNUXtYXXFED5B3YAKxmBAc4u1CqvYC");

        // Start up a basic app using a class that automates some boilerplate.
  /*      kit = new WalletAppKit(params, new File("."), filePrefix);

        if (params == RegTestParams.get()) {
            // Regression test mode is designed for testing and development only, so there's no public network for it.
            // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
            kit.connectToLocalHost();
        }
*/
        // Download the block chain and wait until it's done.
//        kit.startAsync();
//	Thread.sleep(1000);

        /*byte[] genesis_block = MainNetParams.getGenesisBlock();
        StringBuilder sb = new StringBuilder();
        for (byte b : genesis_block) {
              sb.append(String.format("%02X ", b));
        }
        System.out.println(sb.toString());
*/
        
        //final BlockChain chain = kit.chain();
        //final BlockStore store = new MemoryBlockStore(params);
        final BlockStore store = new MemoryFullPrunedBlockStore(params, 100000);
        final BlockChain chain = new BlockChain(params, store);
        final PeerGroup peerGroup = new PeerGroup(params, chain, new BlockingClientManager());

        peerGroup.startAsync();


        long now = new Date().getTime() / 1000;
        peerGroup.setFastCatchupTimeSecs(now);

        final long timeAgo = now - (86400 * 0);
        System.out.println("Checkpointing up to " + Utils.dateTimeFormat(timeAgo * 1000));


        List<String> peerList = new ArrayList<String>();
        peerList.add("192.168.1.23");

        for (Iterator<String> i = peerList.iterator(); i.hasNext();) {
            final InetAddress ipAddress;
            final PeerAddress peerAddress;
            String peerFlag = i.next();
            try {
                ipAddress = InetAddress.getByName(peerFlag);
                peerAddress = new PeerAddress(ipAddress, params.getPort());
                System.out.println("Connecting to " + peerAddress + "...");
                peerGroup.addAddress(peerAddress);
            } catch (UnknownHostException e) {
                System.err.println("Could not understand peer domain name/IP address: " + peerFlag + ": " + e.getMessage());
                //System.exit(1);
                //return;
            }
        }
        peerGroup.downloadBlockChain();
        
        DnsDiscovery dnsDiscovery = new DnsDiscovery(params);
        peerGroup.addPeerDiscovery(dnsDiscovery);

        final TreeMap<Integer, StoredBlock> checkpoints = new TreeMap<Integer, StoredBlock>();

        
        //chain.addListener(listener);
        
        
        
        chain.addNewBestBlockListener(Threading.SAME_THREAD, new NewBestBlockListener() {
            @Override
            public void notifyNewBestBlock(StoredBlock block) throws VerificationException {
                int height = block.getHeight();
                System.out.println(String.format("got new block height=%d",height));
                
                if (height % params.getInterval() == 0 && block.getHeader().getTimeSeconds() <= timeAgo) {
             //   if (height % 100000 == 0 && block.getHeader().getTimeSeconds() <= timeAgo) {
                    System.out.println(String.format("Checkpointing block %s at height %d, time %s",
                            block.getHeader().getHash(), block.getHeight(), Utils.dateTimeFormat(block.getHeader().getTime())));
                    //checkpoints.put(height, block);
                }
                else{
                	System.err.println(String.format("On height %d\n",height));
                }
            }
        });


//        checkState(checkpoints.size() > 0);

        

        StoredBlock curblk = chain.getChainHead();
        int h=curblk.getHeight();
        System.err.println(String.format("On height %d",h));
        // block interval is matched to 2 weeks
        int interval_blocks = 2 * 7*24*60 / 10;
        System.err.println(String.format("block interval = %d",interval_blocks));
        while(1<h){
        	curblk = curblk.getPrev(store);
        	if(curblk == null){
        		h=0;
        		System.err.println("finishing block search");
        	}
        	else{
            	h = curblk.getHeight();
            	
            	if(h % interval_blocks == 0){
            		System.err.println(String.format("On height %d",h));	
                	checkpoints.put(h, curblk);
            	}
        	}
        }
        
        
        final String suffix= "today-suffix";
        final File plainFile = new File("checkpoints" + suffix);
        final File textFile = new File("checkpoints" + suffix + ".txt");

        // Write checkpoint data out.
        writeBinaryCheckpoints(checkpoints, plainFile);
        writeTextualCheckpoints(checkpoints, textFile);

        peerGroup.stop();
        store.close();

        // Sanity check the created files.
        sanityCheck(plainFile, checkpoints.size());
        sanityCheck(textFile, checkpoints.size());

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignored) {}
    }

    private static void writeBinaryCheckpoints(TreeMap<Integer, StoredBlock> checkpoints, File file) throws Exception {
        final FileOutputStream fileOutputStream = new FileOutputStream(file, false);
        MessageDigest digest = Sha256Hash.newDigest();
        final DigestOutputStream digestOutputStream = new DigestOutputStream(fileOutputStream, digest);
        digestOutputStream.on(false);
        final DataOutputStream dataOutputStream = new DataOutputStream(digestOutputStream);
        dataOutputStream.writeBytes("CHECKPOINTS 1");
        dataOutputStream.writeInt(0);  // Number of signatures to read. Do this later.
        digestOutputStream.on(true);
        dataOutputStream.writeInt(checkpoints.size());
        ByteBuffer buffer = ByteBuffer.allocate(StoredBlock.COMPACT_SERIALIZED_SIZE);
        for (StoredBlock block : checkpoints.values()) {
            block.serializeCompact(buffer);
            dataOutputStream.write(buffer.array());
            buffer.position(0);
        }
        dataOutputStream.close();
        Sha256Hash checkpointsHash = Sha256Hash.wrap(digest.digest());
        System.out.println("Hash of checkpoints data is " + checkpointsHash);
        digestOutputStream.close();
        fileOutputStream.close();
        System.out.println("Checkpoints written to '" + file.getCanonicalPath() + "'.");
    }

    private static void writeTextualCheckpoints(TreeMap<Integer, StoredBlock> checkpoints, File file) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
        writer.println("TXT CHECKPOINTS 1");
        writer.println("0"); // Number of signatures to read. Do this later.
        writer.println(checkpoints.size());
        ByteBuffer buffer = ByteBuffer.allocate(StoredBlock.COMPACT_SERIALIZED_SIZE);
        for (StoredBlock block : checkpoints.values()) {
            block.serializeCompact(buffer);
            writer.println(CheckpointManager.BASE64.encode(buffer.array()));
            buffer.position(0);
        }
        writer.close();
        System.out.println("Checkpoints written to '" + file.getCanonicalPath() + "'.");
    }

    private static void sanityCheck(File file, int expectedSize) throws IOException {
        CheckpointManager manager = new CheckpointManager(params, new FileInputStream(file));
        //checkState(manager.numCheckpoints() == expectedSize);

        if (params.getId().equals(NetworkParameters.ID_MAINNET)) {
            StoredBlock test = manager.getCheckpointBefore(1390500000); // Thu Jan 23 19:00:00 CET 2014
            //checkState(test.getHeight() == 280224);
            //checkState(test.getHeader().getHashAsString()
             //       .equals("00000000000000000b5d59a15f831e1c45cb688a4db6b0a60054d49a9997fa34"));
        } else if (params.getId().equals(NetworkParameters.ID_TESTNET)) {
            StoredBlock test = manager.getCheckpointBefore(1390500000); // Thu Jan 23 19:00:00 CET 2014
            //checkState(test.getHeight() == 167328);
            //checkState(test.getHeader().getHashAsString()
            //        .equals("0000000000035ae7d5025c2538067fe7adb1cf5d5d9c31b024137d9090ed13a9"));
        }
    }
}
