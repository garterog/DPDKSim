package simulator;

public abstract class Event {
	
	// ATTRIBUTES
	private double date;
	
	// CONSTRUCTOR
	public Event(double date_) {
		this.date = date_;
	}
	
	// METHODS
	public double getDate(){
		return this.date;
	}
	
	public void schedule(DES simu) {
		simu.insertEvent(this);
	}
	
	public abstract void execute(DES simu);
	
}
