package simulator.queueing.object;

import distribution.*;
import simulator.*;
import simulator.queueing.*;
import simulator.queueing.event.*;

public class SimpleServer extends Server {

	// ATTRIBUTES
	protected Distribution serviceDistribution;
	
	// CONSTRUCTOR
	public SimpleServer(Distribution serviceDistribution_) {
		super();
		this.serviceDistribution = serviceDistribution_;
		
	}
	
	// METHODS
	public void serveQueue(QDES simu, Queue q){
		
		Customer c = q.getFirstWaitingUnholdCustomer();		
		c.setCurrentServer(this);
		this.busy = true;
		c.startHold();
		c.startService();
		double serviceTime = this.serviceDistribution.nextDouble();
		Service serviceEvent = new Service(simu.getSimTime()+serviceTime, c);
		serviceEvent.schedule(simu);
	}
	
	public void processService(QDES simu, Customer c) {
		this.busy = false;
		c.endService();
		this.processDeparture(simu, c);
	}
	
	public void processDeparture(QDES simu, Customer c) {
		
		c.endHold();
		Queue currentQueue = c.getCurrentQueue();
		currentQueue.dequeue(c);
		
		// Notify the departure and the response time
		Notification departureNotification = new Notification(Notification.Type.DEPARTURE, (Double) simu.getSimTime());
		currentQueue.declareNotification(departureNotification);
		
		double responseTime = simu.getSimTime() - c.getArrivalTime();
		Notification responseTimeNotification = new Notification(Notification.Type.RESPONSE_TIME, responseTime);
		currentQueue.declareNotification(responseTimeNotification);
		
		this.switchQueue(simu, currentQueue);
		
	}
	
}
