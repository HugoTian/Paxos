package message;

import java.util.LinkedList;
import java.util.List;

import value.Constant;

/**
 * blocking queues
 * a queue which is thread safe to put into, and take instances from
 */
public class MessageFIFO {
  private List<Message> queue = new LinkedList<Message>();
  private int size;

  public MessageFIFO() {
	// TODO Auto-generated constructor stub
    size  =  Constant.FIFOSIZE;
  }
  
  /**
   * enqueue for blocking queues
   * threads waiting to enqueue items if queue is full.
   */
  public synchronized void enqueue(Message msg) {
    if (queue.size() == size) 
    	return;
    queue.add(msg);
  }

  /**
   * dequeue for blocking queues
   * threads waiting to dequeue items if queue is empty.
   */
  public synchronized Message dequeue() {
	/*
    while (this.queue.size() == 0) {
      waitForQueue();
    }
    if (queue.size() == size) {
      notifyAll();
    }
    */
    if(queue.size() > 0)
    	return queue.remove(0);
    else{
    	return null;
    }
  }

  /**
   * wait function for blocking queue
  
  public void waitForQueue() {
    try {
      wait();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  */
}
