package simulator;

import java.util.Observable;
import java.util.Observer;

public abstract class SimObject extends Observable  implements Observer {

	public abstract void init(DES simu);
	
	public void declareNotification(Notification notification) {
		this.setChanged();
		this.notifyObservers(notification);
	}
	
}
