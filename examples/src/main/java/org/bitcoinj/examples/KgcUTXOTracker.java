package org.bitcoinj.examples;


import org.bitcoinj.core.*;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.FullPrunedBlockStore;
import org.bitcoinj.store.H2FullPrunedBlockStore;
import org.bitcoinj.store.PostgresFullPrunedBlockStore;
//import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ForwardingService demonstrates basic usage of the library. It sits on the network and when it receives coins, simply
 * sends them onwards to an address given on the command line.
 */

public class KgcUTXOTracker {
	private static enum Mode
    {
    	MAINNET,
    	TESTNET,
    	REGTEST
    };
    private static Mode mode = Mode.MAINNET;
    private static String database_path;
    private static NetworkParameters params;
    public static WalletAppKit walletKit;
    
    private static String db_host;
    private static String db_name;
    private static String db_user;
    private static String db_password;
    
    private static FullPrunedBlockStore store;
	private static PeerGroup peer_group;
	
	private static String chain = "segwit2x";

    public static void main(String[] args) throws Exception {
        // This line makes the log output more compact and easily read, especially when using the JDK log adapter.
        //BriefLogFormatter.init();
        if (args.length < 1) {
            System.err.println("Usage: address-to-send-back-to [mainnet|regtest|testnet]");
            return;
        }
        db_host = System.getenv("DBHOST");
        db_name = System.getenv("DBNAME");
        db_user = System.getenv("DBUSER");
        db_password = System.getenv("DBPASSWORD");

        
        switch (args[0])
        {
        	case "mainnet":
        		params  = MainNetParams.get();
        		mode = Mode.MAINNET;
        		break;
        	case "testnet":
        		params = TestNet3Params.get();
        		mode = Mode.TESTNET;
        		break;
        	default: // REGTEST
        		params = RegTestParams.get();
        		mode = Mode.REGTEST;
        		break;
        }
        
        ChainSetup();
        ChainDownload();

    }

   
    
    private static void ChainSetup(){
    	System.err.println("Setting up chain");
    	try{
    		FullPrunedBlockStore store = new PostgresFullPrunedBlockStore(params,2000,db_host,db_name,db_user,db_password,chain);
    		FullPrunedBlockChain vChain = new FullPrunedBlockChain(params,store);
        	vChain.setRunScripts(false);
        	peer_group = new PeerGroup(params, vChain);
        	
    	} catch(BlockStoreException e){
    		
    		System.err.print(String.format("error:%s",e.toString()));
    		System.exit(1);
    	}
    }
    
    private static void ChainDownload(){
    	System.err.println("Downloading chain");
    	peer_group.downloadBlockChain();
    }
}