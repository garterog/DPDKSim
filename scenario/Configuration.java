package scenario;

public class Configuration {

	public static double SIMULATION_TIME = 500000;
	public final static String TIME_UNIT = "µs";
	public static double SWITCHRATE = 1000;
	public static int BATCH_SIZE = 16;
	public static int N = 4;
	public static int K = 128;
	public static double LAMBDA = 5.5;
	public static double TM = 0.03;
	public static double TH = 0.0933;
	public static double TR = 0.0625;
	
	public static void printDetails() {
		System.out.println("*****************************************************************");
		System.out.println("* DPDKSim - A simulator for DPDK-based Virtual Switches (1.0.0) *");
		System.out.println("*****************************************************************");
	}

}
