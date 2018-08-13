package simulator.queueing.object;

import java.util.Iterator;
import java.util.Vector;
import java.util.Random;

import scenario.Configuration;
import simulator.*;
import simulator.queueing.*;
import simulator.queueing.event.*;

public class BatchServer extends HeterogeneousServer {

	
	// CONSTRUCTOR
	public BatchServer()
	{
		super();
	}
	
	public void serveQueue(QDES simu, Queue q)
	{
		
		//notification for calculating the proba instant
		Notification numberOfCustomer = new Notification(Notification.Type.WAITING_SIZE, q.getNbCustomers());
		q.declareNotification(numberOfCustomer);
				
		this.busy = true; //set the server busy, blocking the server;
		
		double serviceTimeBatch = 0; //the total time of service for a batch of packets
		int batchsize = 0;
		
		Vector<Vector<BatchCustomer>> subcustomers = new Vector<Vector<BatchCustomer>>();
		for(int i = 0; i < Configuration.N -1 ; i++)//initialization
		{
			subcustomers.addElement(new Vector<BatchCustomer>());
		}
		
		for(int i = 0; i < Configuration.BATCH_SIZE ;i++)
		{			

			//there is no more clients in the queue
			if (q.getFirstWaitingUnholdCustomer() == null) break;
			
			//get the first unhold clients in the queue
			BatchCustomer c = (BatchCustomer)q.getFirstWaitingUnholdCustomer();
			c.startHold();//important
			c.setCurrentServer(this);//set current server
			int export = c.getExport();
			
			//choose the service Time
			if (subcustomers.get(export-1).size() == 0) c.setServiceTime(c.getServiceTimeMiss());// the first one
			else c.setServiceTime(c.getServiceTimeHit());// the second one;
			serviceTimeBatch += c.getServiceTime();
			
			//Add customer to subcustomer
			subcustomers.get(export-1).add(c);
			batchsize ++;
			
		}
		
		ServiceBatch serviceEvent = new ServiceBatch(simu.getSimTime() + serviceTimeBatch, subcustomers, this, q);
		serviceEvent.schedule(simu);
		
		Notification batchevent = new Notification(Notification.Type.BATCH_SIZE, batchsize);
		q.declareNotification(batchevent);
				
	}
	
	public void processService(QDES simu, Vector<Vector<BatchCustomer>> customers, Queue q)
	{
		Vector<BatchCustomer> last = new Vector<BatchCustomer>();
		double rt= 0;//releasing time
		double tt = 0; //total time : releasing time + service time
		double size = 0;
		
		for(int i = Configuration.N -2 ; i >= 0 ; i--)
		{
			if(customers.get(i).size() != 0)
			{
				last = customers.get(i);
				break;
			}
		}
		for(Vector<BatchCustomer> sub : customers)
		{
			//no client in one of exports
			if(sub.size() == 0) continue;
			
			for(BatchCustomer c : sub)
			{
				rt +=  c.getServiceTimeReleasing();
				tt += c.getServiceTimeReleasing() + c.getServiceTime();
				size++;
			}
			boolean ifLast = false;
			if(sub == last) ifLast = true;
			Departure departureEvent = new Departure(simu.getSimTime() + rt, sub, this, q, ifLast);
			departureEvent.schedule(simu);	
	
		}
		
		//get the average all the service time 
		if (size != 0)
		{
			Notification serviceBatchNotification = new Notification(Notification.Type.SERVICE_TIME_BATCH, tt/size);
			q.declareNotification(serviceBatchNotification);
		}
	}
	
	
	public void processDeparture(QDES simu, Vector<BatchCustomer> subcustomers, Queue q, boolean last)
	{
		// note that after the last one, switch to next queue
		Iterator<BatchCustomer> it = subcustomers.iterator();
		
		while(it.hasNext())
		{
			BatchCustomer c = it.next();
			c.endHold();
			c.endService();
			
			q.dequeue(c);
			
			//Notify the departure and the response time
			Notification departureNotification = new Notification(Notification.Type.DEPARTURE, (Double) simu.getSimTime());
			q.declareNotification(departureNotification);
			
			double responseTime = simu.getSimTime() - c.getArrivalTime();
			Notification responseNotification = new Notification(Notification.Type.RESPONSE_TIME, responseTime);
			q.declareNotification(responseNotification);
			
			//all service time (lookup time and releasing time)
			double serviceTime = c.getServiceTime() + c.getServiceTimeReleasing();
			Notification serviceNotification = new Notification(Notification.Type.SERVICE_TIME, serviceTime);
			q.declareNotification(serviceNotification);
		}
	
		if(last)
		{
			this.busy = false;
			this.switchQueue(simu, q);
		}
		

	}
}