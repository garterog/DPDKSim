package scenario;

import simulator.queueing.*;
import simulator.queueing.object.*;
import distribution.*;
import java.util.Vector;

public class ExampleScenario {

	/**
	 * Main method of the example scenario
	 */
	public static void main(String[] args) {
		
		Configuration.printDetails();

		// Parameters from arguments
		int args_index = 0;
		for (String s: args) { // does not include the name of the main class
                       
			if (s.compareTo("-N") == 0) { // the number of port
				Configuration.N = Integer.parseInt(args[args_index+1]);
			}
			
			if (s.compareTo("-K") == 0) { // max queue size (capacity)
				Configuration.K = Integer.parseInt(args[args_index+1]);
            }
            
			if (s.compareTo("-LOAD") == 0) { // Global arrival rate
				Configuration.LAMBDA = Double.parseDouble(args[args_index+1]);
            }
			
			if (s.compareTo("-TSIM") == 0) { // simulation time
				Configuration.SIMULATION_TIME = Double.parseDouble(args[args_index+1]);
            }
			
			if (s.compareTo("-TH") == 0) { // TH time
				Configuration.TH = Double.parseDouble(args[args_index+1]);
            }

            if (s.compareTo("-TM") == 0) { // TM time
				Configuration.TM = Double.parseDouble(args[args_index+1]);
            }

            if (s.compareTo("-TR") == 0) { // TR time
				Configuration.TR = Double.parseDouble(args[args_index+1]);
            }

            if (s.compareTo("-BATCHSIZE") == 0) { // batch size
				Configuration.BATCH_SIZE = Integer.parseInt(args[args_index+1]);
            }

			args_index++;
        }
		
		// Display parameters

		int N = Configuration.N;
		int K = Configuration.K;
		double Lambda = Configuration.LAMBDA;
		double timeH = Configuration.TH;
		double timeM = Configuration.TM;
		double timeR = Configuration.TR;
		int batchSize = Configuration.BATCH_SIZE;

		System.out.println("* TSIM = "+Configuration.SIMULATION_TIME+" "+Configuration.TIME_UNIT);
		System.out.println("* K = "+K);
		System.out.println("* N = "+N);
		System.out.println("* LOAD = "+Lambda);
		System.out.println("* BATCHSIZE = "+ batchSize);
		System.out.println("* TH = "+timeH);
		System.out.println("* TR = "+timeR);
		System.out.println("* TM = "+timeM);
		
		// Create the queues
		Queue[] queues = new Queue[N];
		for (int k=0; k<N ; k++) {
			queues[k] = new Queue(K);
		}
		
		
		// Create the CPU server
		double muh_0 = 1/timeH;
		double mum_0 = 1/timeM;
		double mur_0 = 1/timeR;
		
		Distribution[] serviceHit = new Distribution[N];
		Distribution[] serviceMiss = new Distribution[N];
		Distribution[] release = new Distribution[N];
		
		for (int i = 0 ; i < N ; i++)
		{
			serviceHit[i] = new Exponential(muh_0);
			serviceMiss[i] = new Exponential(mum_0);
			release[i] = new Exponential(mur_0);
		}
		
		
		Server server = new BatchServer();

	
		// Create the traffic sources		
		// Create the arrival distributions and associated sources
		
		double[] lambdas = new double[N];
		double[] probas = new double[N];

		// Uniform repartition example
		for(int i = 0 ; i < N ;i++)
		{
			probas[i] = 1.0/N;
		}

		
		for(int i = 0 ; i < N ;i++)
		{
			lambdas[i] = probas[i]*Lambda;
		}
	
		
		Distribution[] arrival = new Distribution[N];
		
		for(int i = 0; i < N ; i++)
		{
			arrival[i] = new Exponential(lambdas[i]);
		}	
	
		Source[] sources = new Source[N]; // Create N sources with Poisson arrivals
		for (int k=0; k<N ; k++) {
			sources[k] = new BatchSource(arrival[k], serviceHit[k], serviceMiss[k],release[k]);
		}
			
		// Create the simulator
		QDES simu = new QDES();
		
		// Register the simulation objects
		for (int k=0; k<N ; k++) {
			simu.register(sources[k]); // here the sources
			simu.register(queues[k]); // here the queues
		}
		simu.register(server); // here the polling server/processor
		
		// Attach the simulation objects with each others
		for (int k=0; k<N ; k++) {
			simu.attach(sources[k], queues[k]); // each source sends traffic to a specific queue
			simu.attach(queues[k], server); // each queue is served by the server in a polling fashion
		}
		
		// Initialize the simulation
		simu.init();
		
		// Run the simulation
		simu.run();
			
		// Collect and display the simulation results
		simu.getResults();
		
	}
}