package application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;

import value.Constant;
import framework.Config;
import framework.NetController;
import message.Message;
import message.ResponseMessage;
/**
 * The class for paxos environment
 * @author Tian Zhang
 */
public class Paxos {
		public Server[] serverList;
		public Client[] clientList;
		public Config config;
		public NetController ncController;
		/**
		 * Default Constructor
		 * 
		 */
		public Paxos() {
			// TODO Auto-generated constructor stub
		}
		
		
		/**
		 * Config the paxos environment
		 * @param numServers, number od client
		 */
		public Paxos(int numServers, int numClients) {
			serverList = new Server[numServers];
		    for (int i = 1; i <= numServers; i++) {
		      serverList[i - 1] = new Server(i, numServers, numClients, this, false);
		    }

		    clientList = new Client[numClients];
		    for (int i = 0; i < numClients; i++) {
		      clientList[i] = new Client(i, numServers, numClients, this);
		      clientList[i].start();
		    }
		    //
		    for (int i = numServers - 1; i >= 0; i--) {
		      serverList[i].start();
		    }
		   
		    
					
		}
		
		/**
		 * print the log of client
		 * @param id of client
		 */
		
		public String printLog(int clientID){
			return clientList[clientID].printChatLog();
		}
		
		
		/**
		 * crash the server
		 * @param id of server
		 */
		
		public void crashServer(int serverID){
			 
			  serverList[serverID].cleanShutDown();
		}
		
		
		/**
		 * restart the server
		 * @param id of server
		 */
		
		public void reviveServer(int serverID){
			serverList[serverID] = new Server(serverID + 1, serverList.length, clientList.length, this,true);
			serverList[serverID].recover();
		    serverList[serverID].start();  
		}
		
		/**
		 * a client broadcast the message
		 * @param id of client
		 */
		
		public void clientBroadcast(int client, String Msg ){
			  clientList[client].broadcast(Msg);
			  
		}
		
		
		
		
}
