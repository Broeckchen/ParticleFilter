
public class Particle {

	private double position;
	private double weight;
	
	public Particle(double position, double weight){
		this.position = position;
		this.weight = weight;
	}
	
	public double getPosition() {
		return position;
	}
	public void setPosition(double position) {
		this.position = position;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
}
