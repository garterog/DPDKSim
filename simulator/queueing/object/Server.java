package simulator.queueing.object;

import simulator.*;
import simulator.queueing.QDES;
import simulator.queueing.entry.ServerEntry;

import java.util.Observable;

public class Server extends SimObject {

	// ATTRIBUTES
	protected static int nbServersGenerated = 0;
	protected int id;
	protected boolean busy;
	
	// CONSTRUCTOR
	public Server() {
		this.id = (++nbServersGenerated);
		this.busy = false;
	
	}
	
	// METHODS
	public int getId() {
		return this.id;
	}
	
	public boolean isBusy() {
		return this.busy;
	}

	public void switchQueue(QDES simu, Queue currentQueue) {
		
		// Start vacation
		if (currentQueue.getFirstWaitingUnholdCustomer() != null && !currentQueue.inVacation()) { // At least one customer waiting to be served
			currentQueue.startVacation(simu);
		}
		
		ServerEntry entry = simu.lookAt(this); // look at the next queue to be served
		Queue nextQueue = entry.getNextQueueToServe(currentQueue);
		
		if (nextQueue.getFirstWaitingUnholdCustomer() != null) { // Serve the customer

			// At least one customer waiting to be served
			nextQueue.endVacation(simu);
			this.serveQueue(simu, nextQueue);
			
		} else
		{
			//notification for calculating the proba instant
			Notification numberOfCustomer = new Notification(Notification.Type.WAITING_SIZE, currentQueue.getNbCustomers());
			currentQueue.declareNotification(numberOfCustomer);
			
			if (entry.anyQueueWaitingProcessing()) {

				// Program a switching event
				this.switchQueue(simu, nextQueue);

			} else { // Other queues empty => totally empty system

			}
		}
	}
	
	public void processService( QDES simu, Customer c) {
	}
	
	public void processDeparture( QDES simu, Customer c){
	}
	
	
	public void serveQueue( QDES simu, Queue q) {
	}
	
		
	public String toString() {
		return "Server "+this.id;
	}
	
	@Override
	public void init(DES simu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
	
	
}
