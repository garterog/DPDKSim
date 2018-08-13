package simulator.queueing;

import simulator.*;
import simulator.queueing.object.*;
import simulator.queueing.entry.*;

import java.util.LinkedHashMap;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import scenario.Configuration;

public class QSimObjects implements SimObjects {

	// ATTRIBUTES
	Map<Source, SourceEntry> sources;
	Map<Queue, QueueEntry> queues;
	Map<Server, ServerEntry> servers;
		
	// CONSTRUCTOR
	public QSimObjects() {
		this.sources = new LinkedHashMap<Source, SourceEntry>();
		this.queues = new LinkedHashMap<Queue, QueueEntry>();
		this.servers = new LinkedHashMap<Server, ServerEntry>();
	}

	@Override
	public void init(DES simu) {
		// Initialize the sources
		Iterator<Source> sourcesIter = this.sources.keySet().iterator();
		while (sourcesIter.hasNext()) {
			sourcesIter.next().init((QDES) simu);
		}
		
		// Initialize the queues
		Iterator<Queue> queuesIter = this.queues.keySet().iterator();
		while (queuesIter.hasNext()) {
			queuesIter.next().init((QDES) simu);
		}
				
		// Initialize the servers
		Iterator<Server> serversIter = this.servers.keySet().iterator();
		while (serversIter.hasNext()) {
			serversIter.next().init((QDES) simu);
		}
	}

	public void register(Source source) {
		if (!this.sources.containsKey(source)) {
			SourceEntry entry = new SourceEntry(source);
			this.sources.put(source, entry);
			source.addObserver(entry);
		}
	}
	
	public void register(Queue queue) {
		if (!this.queues.containsKey(queue)) {
			QueueEntry entry = new QueueEntry(queue);
			this.queues.put(queue, entry);
			queue.addObserver(entry);
		}
	}
	
	public void register(Server server) {
		if (!this.servers.containsKey(server)) {
			ServerEntry entry = new ServerEntry(server);
			this.servers.put(server, entry);
			server.addObserver(entry);
		}
	}
	
	public SourceEntry lookAt(Source source) {
		if (this.sources.containsKey(source)) {
			return this.sources.get(source);
		} else {
			return null;
		}
	}
	
	public QueueEntry lookAt(Queue queue) {
		if (this.queues.containsKey(queue)) {
			return this.queues.get(queue);
		} else {
			return null;
		}
	}
	
	public ServerEntry lookAt(Server server) {
		if (this.servers.containsKey(server)) {
			return this.servers.get(server);
		} else {
			return null;
		}
	}
	
	public void attach(Source source, Queue queue) { // single attachment
		this.sources.get(source).attach(queue);
	}
	
	public void attach(Queue queue, Server server) { // double attachment
		this.queues.get(queue).attach(server);
		this.servers.get(server).attach(queue);
		queue.addObserver(server);
	}
	
	@Override
	public void getResults() {
		
		// Look at results of the sources
		Iterator<Source> sourcesIter = this.sources.keySet().iterator();
		while (sourcesIter.hasNext()) {
			this.sources.get(sourcesIter.next()).getResults();
		}
				
		// Look at results of the queues
		Iterator<Queue> queuesIter = this.queues.keySet().iterator();
		while (queuesIter.hasNext()) {
			this.queues.get(queuesIter.next()).getResults();
		}
		
		// Look at results of the servers
		Iterator<Server> serversIter = this.servers.keySet().iterator();
		while (serversIter.hasNext()) {
			this.servers.get(serversIter.next()).getResults();
		}
				
	}
	
	@Override
	public void traceResults(String traceFile) {
		
		// Trace the performances achieved in the simulation
		// Trace format: Q_1 B_1 .. Q_N B_N
		String outputString = "";
		
		String outputStringDist = "";
		
		double Q;
		double B;
		double S; //standard of queue size
		double T; //throughput
		
		Iterator<Queue> queuesIter = this.queues.keySet().iterator();
		Queue q;
		while (queuesIter.hasNext()) {
			q = queuesIter.next();
			Q = this.lookAt(q).getAverageNbCustomers();
			B = this.lookAt(q).getBlockingProbability();
			T = this.lookAt(q).getThroughput();
			outputString = outputString +Q+" "+B+" "+T+"  ";
		}
		
		queuesIter = this.queues.keySet().iterator();
		while(queuesIter.hasNext())
		{
			q = queuesIter.next();
			outputString += this.lookAt(q).getAverageNbClientInstant() + " ";
		}

		String fileAddr = System.getProperty("user.dir") + "/traces/"+ traceFile;
		
		try
		{
			boolean erase = false; //true;
			FileWriter fw = new FileWriter(fileAddr, !erase);
			BufferedWriter output = new BufferedWriter(fw);
			
			output.write(outputString+"\n");
			
			output.flush(); // Send to the file
			output.close();
				
		}
		catch(IOException ioe){
			System.out.print("Error: ");
			ioe.printStackTrace();
		}
		
						
	}
	
	
}
