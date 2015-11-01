package application;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import message.Message;
import message.Phase1aMessage;
import message.Phase2aMessage;
import role.Acceptor;
import role.Leader;
import role.NodeRole;
import role.Replica;
import value.Command;
import value.Constant;

/**
 * @author zhangtian
 */



public class Server extends Node {
  
  // Server identifier
  public int index;
  // the id of current leader
  public int leaderID;
  // whether the server is working
  public boolean shutdown = false;

 
  /**
   * Local server process id
   * Reserved ID :
   * Replica : 0
   * Acceptor : 1
   * Leader : 2
   */
  public int id = 3;

  /**
   * For time bomb leader
   */
  ReentrantLock lock ;
  public int messageCount = -1;

  /**
   * Map from process id to NodeRole
   */
  public HashMap<Integer, NodeRole> roles;

  Replica replica;
  Acceptor acceptor;
  Leader leader;
   
  /**
 	 * Default constructor for Server
 	 * @param Id, number of server, number of client, the paxos environment, whether it is recover thread
 	 */

  public Server(int idx, int numSevers, int numClients, Paxos paxos,boolean restart) {
    super(paxos, idx, numSevers, numClients,false);
    index = idx;
    roles = new HashMap<Integer, NodeRole>();
    lock = new ReentrantLock();
    if (!restart) {
      leaderID = 1;
      /* initiates replica */
      Map<Integer, Command> committed = new HashMap<Integer, Command>();
      int rpid = bind(index, Constant.REPLICA);
      replica = new Replica(rpid, this, committed, 0);

      initialization();
    }
  }
  
  /**
	 * recover the server
	 * @param none
	 */
  public void recover () {
   
  }

  /**
 	 * initialize the server
 	 * @param none
 	 */
  public void initialization () {
      /* initiates acceptor */
    acceptor = new Acceptor(bind(index, Constant.ACCEPTOR), this);

    if (isLeader()) {
      leader = new Leader(bind(index, Constant.LEADER), this, acceptors, replicas);
    } else {
      leader = null;
    }

   
 }

  /**
	 * start the server
	 * @param none
	 */

  public void run () {
    acceptor.start();
    replica.start();
    if (isLeader()) {
      leader.start();
    }
    


    while (!shutdown) {
      Message msg = receive();
      if (shutdown) {
        return;
      }
      if (msg != null) {
        relay(msg);
      }
    }
  }

  /**
	 * return whether is leader
	 * @param none
	 */
  public boolean isLeader() {
    return leaderID == index;
  }

  /**
	 * get the lead id
	 * @param none
	 */
  public int getLeader () {
    return bind(leaderID, Constant.LEADER);
  }

  /**
	 * return the id of changing role
	 * @param none
	 */
  public synchronized int nextId() {
    
    return bind(index, id++);
  }

  /**
   *  Relay message to local process
   */
  public void relay(Message msg) {
    if (roles.containsKey(msg.dst)) {
      roles.get(msg.dst).deliver(msg);
      //System.out.println(msg.print());
    } 
  }

  /**
   *  remove the role
   */

  public void remove(int pid) {
    roles.remove(pid);
  }


  

  /**
   * Leader election with basic round robin fashion
   */
  public void leaderElection () {
    leaderID++;
    if (leaderID > numServers) {
      leaderID = 1;
    }
    if (isLeader()) {
      leader = new Leader(bind(index, Constant.LEADER), this,
          acceptors, replicas);
      leader.start();
    } else {
      leader = null;
    }
  }


  /**
   * Clean shutdown the server, including all the
   * Replica, Acceptor, Leader
   */
  public void cleanShutDown () {
    shutdown = true;
  }


  /**
   * time bomber message
   * @param number of messages
   */
  public void timeBombLeader (int msgCount) {
	  lock.lock();
	    if (isLeader()) {
	      if (msgCount == 0) {
	        cleanShutDown();
	      } else {
	        messageCount = msgCount;
	      }
	    }
	    
	 
  	 lock.unlock();
  }
  
  /**
   * Send message 
   * to support time bomb leader function from paxos
   * @param msg
   */
   @Override
   public void send(Message msg) {
	   // count the number which is sent by leader
	   // count down only for phase 1a message and phase 2a message
	   if (msg instanceof Phase1aMessage || msg instanceof Phase2aMessage) {
		   lock.lock();
		   // should shut down now
		   if (messageCount == 0) {
			   if(Constant.DEBUG)
				   System.out.println("Shutdown Now!");
			   cleanShutDown();
			   return;
		   }
		   // count down message
		   else if (messageCount > 0) {
			   messageCount--;
			   if(Constant.DEBUG)
				   System.out.println("Shutdown timer: " + messageCount);
		   }
		   lock.unlock();
	   }
   // Normal send function 
   paxos.send(msg);
 }

  
}
