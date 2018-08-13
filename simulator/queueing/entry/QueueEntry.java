package simulator.queueing.entry;

import simulator.*;
import simulator.queueing.object.*;
import scenario.*;

import java.util.Iterator;
import java.util.Observable;
import java.util.Vector;
import java.math.*;

public class QueueEntry extends SimObjectEntry {

	// ATTRIBUTES
	// The associated simulation objects are servers
	
	private int nbArrivals;
	private int nbBlocked;
	private int nbDepartures;
	private int nbServices;
	private int nbServicesBatch;
	
	private double averageNbCustomers;
	private double standardNbCustomers;
	private double lastArrivalDepartureTime;
	private int lastNbCustomers;
	
	private double averageResponseTime;
	private Vector<Double> responseTimes;
	
	private double averageServiceTime;
	private Vector<Double> serviceTimes;
	
	private double averageServiceTimeBatch;
	private Vector<Double> serviceTimesBatch;
	
	private Vector<Double> vacationTimes;
	private double lastVacationStartTime;
	
	private Vector<Integer> batchSizes;
	
	private double[] cumulativeStateTimes;
	
	private int[] customerInstantSize;
	
	
			
	// CONSTRUCTOR
	public QueueEntry(Queue observedQueue) {
		super(observedQueue);
		this.nbArrivals = 0;
		this.nbDepartures = 0;
		this.nbBlocked = 0;
		this.averageNbCustomers = 0;
		this.lastArrivalDepartureTime = 0;
		this.lastNbCustomers = 0;
		this.averageResponseTime = 0;
		this.responseTimes = new Vector<Double>();
		this.serviceTimes = new Vector<Double>();
		this.serviceTimesBatch = new Vector<Double>();
		this.batchSizes = new Vector<Integer>();
		
		this.vacationTimes = new Vector<Double>();
		this.lastVacationStartTime = 0;
		
		this.cumulativeStateTimes = new double[512]; // no more than 511 spaces in the buffer..
		
		
		this.customerInstantSize = new int[129];
	}
		
	// METHODS
	public Server getNextUnoccupiedServer() {
		Iterator<SimObject> iter = this.associatedSimObjects.iterator();
		Server server;
		while (iter.hasNext()) {
			server = (Server) iter.next();
			if (!server.isBusy()) {
				return server;
			}
		}
		return null;
	}
	
	@Override
	public void update(Observable observedQueue, Object notification) {
		
		switch (((Notification) notification).getType()) {
		
			case ARRIVAL:
				this.nbArrivals++;
				
				// Update the average nb of customers in the queue
				double arrivalTime = (Double) ((Notification) notification).getData();
				this.averageNbCustomers = ( this.averageNbCustomers*this.lastArrivalDepartureTime + this.lastNbCustomers*(arrivalTime-this.lastArrivalDepartureTime)  )*1.0/arrivalTime;

				this.cumulativeStateTimes[this.lastNbCustomers] += (arrivalTime-this.lastArrivalDepartureTime);
				
				this.lastArrivalDepartureTime = arrivalTime;
				this.lastNbCustomers = this.lastNbCustomers + 1;
								
				break;
				
			case BLOCKING:
				this.nbBlocked++;
				break;
				
			case DEPARTURE:
				
				// Update the average nb of customers in the queue
				double departureTime = (Double) ((Notification) notification).getData();
				this.averageNbCustomers = ( this.averageNbCustomers*this.lastArrivalDepartureTime + this.lastNbCustomers*(departureTime-this.lastArrivalDepartureTime) )*1.0/departureTime;
				
	
				this.cumulativeStateTimes[this.lastNbCustomers] += (departureTime-this.lastArrivalDepartureTime);
				
				this.lastArrivalDepartureTime = departureTime;
				this.lastNbCustomers = this.lastNbCustomers - 1;
				
				break;
				
			case RESPONSE_TIME:
				
				// Update the average response time
				double responseTime = (Double) ((Notification) notification).getData();
				this.averageResponseTime = (this.averageResponseTime*this.nbDepartures + responseTime)/(this.nbDepartures+1);
				this.nbDepartures++;
				
				this.responseTimes.add(responseTime);
								
				break;
				
			case SERVICE_TIME:
				// update the average service time
				double serviceTime = (Double) ((Notification) notification).getData();
				this.averageServiceTime = (this.averageServiceTime*this.nbServices + serviceTime) / (++this.nbServices);
				
				this.serviceTimes.add(serviceTime);
				
				break;
				
			case SERVICE_TIME_BATCH:
				// update the average service time
				double serviceTimeBatch = (Double) ((Notification) notification).getData();
				this.averageServiceTimeBatch = (this.averageServiceTimeBatch*this.nbServicesBatch + serviceTimeBatch) / (++this.nbServicesBatch);
				
				this.serviceTimesBatch.add(serviceTimeBatch);
				
				break;
				
			case START_VACATION:
				
				this.lastVacationStartTime = (Double) ((Notification) notification).getData();
				
				break;
				
			case END_VACATION:
				
				double vacationEndTime = (Double) ((Notification) notification).getData();
				double vacationDuration = vacationEndTime - this.lastVacationStartTime;
				this.vacationTimes.addElement(vacationDuration);
				
				break;
				
			case BATCH_SIZE:
				int size = (int) ((Notification)notification).getData();
				this.batchSizes.addElement(size);
	
							
			case WAITING_SIZE:
				this.customerInstantSize[(Integer) ((Notification) notification).getData()]++;
			
			default:
				break;				
		}
		
	}

	public double getAverageVacationTime() {
		double avVacationTime = 0;
		for(int k=0; k<this.vacationTimes.size(); k++) {
			avVacationTime+=this.vacationTimes.get(k);
		}
		avVacationTime = avVacationTime/this.vacationTimes.size();
		return avVacationTime;
	}
	
	public double getAveragePositiveVacationTime() {
		double avVacationTime = 0;
		int count_zero = 0;
		for(int k=0; k<this.vacationTimes.size(); k++) {
			avVacationTime+=this.vacationTimes.get(k);
			if (this.vacationTimes.get(k)<0.0000001) {
				count_zero++;
			}
		}
		avVacationTime = avVacationTime/(this.vacationTimes.size()-count_zero);
		return avVacationTime;
	}
	
	
	public double getVCVacationTime() {
		double varVacationTime = 0;
		double avVacationTime =  this.getAverageVacationTime();
		for(int k=0; k<this.vacationTimes.size(); k++) {
			varVacationTime+=Math.pow((this.vacationTimes.get(k)-avVacationTime),2);
		}
		varVacationTime = varVacationTime/(this.vacationTimes.size()-1);
		double vc = Math.sqrt(varVacationTime)/avVacationTime;
		return vc;
	}
	
	
	public double getVCPositiveVacationTime() {
		double varPositiveVacationTime = 0;
		double avPositiveVacationTime =  this.getAveragePositiveVacationTime();
		int count_zero = 0;
		for(int k=0; k<this.vacationTimes.size(); k++) {
			if (this.vacationTimes.get(k)>0.0000001) {
				varPositiveVacationTime+=Math.pow((this.vacationTimes.get(k)-avPositiveVacationTime),2);
			} else {
				count_zero++;
			}
		}
		varPositiveVacationTime = varPositiveVacationTime/(this.vacationTimes.size()-(1+count_zero));
		double vc = Math.sqrt(varPositiveVacationTime)/avPositiveVacationTime;
		return vc;
	}
	
	
	public double getAverageResponseTime() {
		double avResponseTime = 0;
		for(int k=0; k<this.responseTimes.size(); k++) {
			avResponseTime+=this.responseTimes.get(k);
		}
		avResponseTime = avResponseTime/this.responseTimes.size();
		return avResponseTime;
	}
	
	
	public double getAverageServiceTimeBatch()
	{	
		
		double avServiceTime = 0;
		for(int k=0; k < this.serviceTimesBatch.size() ; k++)
		{
			avServiceTime += this.serviceTimesBatch.get(k);
		}
		avServiceTime /= this.serviceTimesBatch.size();
		
		
		return avServiceTime;
		
	}
	
	public double getAverageServiceTime()
	{	
		
		double avServiceTime = 0;
		for(int k=0; k < this.serviceTimes.size() ; k++)
		{
			avServiceTime += this.serviceTimes.get(k);
		}
		avServiceTime /= this.serviceTimes.size();
		return avServiceTime;
		
	}
	
	public double[] getProbablityInstante()
	{
		double[] probas = new double[129];
		double sum = 0;
		for(int k = 0 ; k <= 128; k++)
		{
			sum += this.customerInstantSize[k];
		}
		
		for (int k = 0 ; k<= 128 ; k++)
		{
			probas[k] = this.customerInstantSize[k]/sum;
		}
		return probas; 
		
	}
	
	
	
	public double getAverageNbClientInstant()
	{
		double probasinstant[] = this.getProbablityInstante();
		double average = 0 ;
		for(int k = 0 ; k <= 128 ; k++)
		{
			average += k*probasinstant[k];
		}
		
		return average;
	}
	
	public double getBatchSize()
	{
		double sum = 0;
		for(int v : this.batchSizes)
		{
			sum += v;
		}
		
		return sum / this.batchSizes.size();
	}
	public double getVCResponseTime() {
		double varResponseTime = 0;
		double avResponseTime =  this.getAverageResponseTime();
		for(int k=0; k<this.responseTimes.size(); k++) {
			varResponseTime+=Math.pow((this.responseTimes.get(k)-avResponseTime),2);
		}
		varResponseTime = varResponseTime/(this.responseTimes.size()-1);
		double vc = Math.sqrt(varResponseTime)/avResponseTime;
		return vc;
	}
	
	public double getBlockingProbability() {
		return this.nbBlocked*1.0/(this.nbArrivals+this.nbBlocked);
	}
	
	
	public double getAverageNbCustomers() {
		return this.averageNbCustomers;
	}
	
	
	public double getAverageNbCustomersFromDist() {
		double result = 0;
		double total_time = 0;
		for (int k=0; k<512; k++) {
			total_time += this.cumulativeStateTimes[k];
			result += k*this.cumulativeStateTimes[k];
		}
		
		result = result*1.0/total_time;
		
		return result;
	}
	

	
	public double getThroughput() {
		return this.nbDepartures/this.lastArrivalDepartureTime;
	}
	
	@Override
	public void getResults() {
		
		double vc = getVCResponseTime();
		
		System.out.println("\nSimulation results for "+this.getObservedSimObject());
		System.out.print(" - blocking probability: "+this.getBlockingProbability());
		System.out.print("\n - "+"average number of cust.: "+this.averageNbCustomers+"\n - "+"average response time: "+this.averageResponseTime+" "+Configuration.TIME_UNIT+" with vc = "+vc);
		System.out.print("\n - "+"Throughput : "+this.getThroughput() + "\n");
	
	}
	
}
