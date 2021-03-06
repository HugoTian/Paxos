package role;


import java.util.HashSet;
import message.DecisionMessage;
import message.Message;
import message.Phase2aMessage;
import message.Phase2bMessage;
import message.PreemptedMessage;

import application.Server;

import value.BallotNum;
import value.Pvalue;

/**
 * @author zhangtian
 */
public class Commander extends NodeRole {
  int lambda;
  int[] acceptors;
  int[] replicas;
  Pvalue pv;
  BallotNum b;
  HashSet<Integer> waitingList = new HashSet<Integer>();

  public Commander(int pid, Server ctrl, int lambda, int[] acceptors,
                   int[] replicas, Pvalue p) {
    super(pid, ctrl);
    this.lambda = lambda;
    this.acceptors = acceptors;
    this.replicas = replicas;
    this.pv = p;
    this.b = p.ballotNum;

    server.roles.put(pid, this);
  }

  @Override
  public void execute() {
    for (int acpt : acceptors) {
      waitingList.add(acpt);
      send(acpt, new Phase2aMessage(pid, pv));
    }
    // while the server is working correctly
    while (!server.shutdown) {
      if(server.shutdown){
    	  return;
      }
      Message msg = receive();
     
      if (msg instanceof Phase2bMessage) {
    	
        Phase2bMessage p2b = (Phase2bMessage) msg;
        if (b.compareTo(p2b.ballotNum) == 0) {
          if(waitingList.contains(p2b.src)) {
            waitingList.remove(p2b.src);
          }
         // System.out.println("I am receive phase 2b message" + msg  + " and the size is  " + waitingList.size());
          if (waitingList.size() < (acceptors.length + 1) / 2) {
            for (int p : replicas) {
              Message decision = new DecisionMessage(pid, pv.slotNum, pv.prop);
              send(p, decision);
            }
            return; 
          }
        } else {
          send(lambda, new PreemptedMessage(pid, p2b.ballotNum));
          return; 
        }
      }
    }
  }

  
}
