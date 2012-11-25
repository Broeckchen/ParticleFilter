import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.math3.distribution.NormalDistribution;

public class ParticleFilter {

	public static final double RELIABILITY = 0.9;
	public static final double STANDARD_DEVIATION = 0.3;
	public static final double MOVE_DEVIATION_FACTOR = 0.15;
	public static final NormalDistribution PRIOR = new NormalDistribution(0, STANDARD_DEVIATION);
	public static final double[] doors = {1.0, 2.5, 5, 7};
	
	public static final double ZOOM_FACTOR = 20;
	public static final int TERMINATION_LIMIT= 10000;
	
	private int step;
	private List<Particle> particles;
	private int amount;
	private double rangeStart;
	private double rangeEnd;

	public ParticleFilter() {
		this.particles = new ArrayList<Particle>();
		step = -1;
	}

	public void initialize(double rangeStart, double rangeEnd, int amount) {
		step = 0;
		particles.clear();
		Random r = new Random();
		double weight = 1.0 / amount;
		for (int i = 0; i < amount; i++) {
			particles.add(new Particle(r.nextDouble() * 10, weight));
		}
		this.amount = amount;
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
	}
	
	public void sensorStep(boolean doorDetected){
		step++;
		if(doorDetected){
			double normalisationQuotient = 0.0;
			for(Particle p: particles){
				double distance = findDoorDistance(p.getPosition());
				double prior = PRIOR.density(distance);
				double newWeight = p.getWeight()*(RELIABILITY*prior + (1-RELIABILITY)*(1-prior));
				p.setWeight(newWeight);
				normalisationQuotient += newWeight;
			}
			System.out.println(normalisationQuotient);
			for(Particle p: particles){
				p.setWeight(p.getWeight()/normalisationQuotient);
				System.out.println(p.getWeight());
			}
		}else{
			//unknown
		}
	}
	
	public double findDoorDistance(double position){
		double minDistance = 10.0;
		for(double doorPosition: doors){
			double distance = Math.abs(doorPosition-position);
			if(distance<minDistance){
				minDistance = distance;
			}
		}
		return minDistance;
	}
	
	//done using systematic resampling
	public void moveStep(double movement) {
		step++;
		NormalDistribution moveDistribution = new NormalDistribution(movement, Math.abs(MOVE_DEVIATION_FACTOR*movement));
		List<Particle> resampledParticles = new ArrayList<Particle>();
		Random r = new Random();
		double startingPoint = r.nextDouble()/amount;
		double cumulativeWeights=0.0;
		for(Particle p: particles){
			double treshold = p.getWeight()+cumulativeWeights;
			while(resampledParticles.size()*1.0/amount + startingPoint < treshold){
				double newPosition = p.getPosition() + moveDistribution.sample();
				int terminationCounter = 0;
				while(newPosition > 10 || newPosition < 0){
					if(terminationCounter < TERMINATION_LIMIT){
						terminationCounter++;
						newPosition = p.getPosition() + moveDistribution.sample();
					}else{
						if(newPosition<0){
							newPosition = 0;
						}
						if(newPosition>10){
							newPosition = 10;
						}
					}
				}
				resampledParticles.add(new Particle(newPosition, 1.0/amount));
				System.out.println("New Particle: Old position: "+p.getPosition()+", new position: "+newPosition);
			}
			cumulativeWeights = treshold;
		}
		particles = resampledParticles;
	}

	public void saveVisualisation() {
		if (step > -1) {
			int runId = 1;

			File output = new File("output" + File.separator + "Particles_run"
					+ runId + "_step" + step + ".png");
			while (output.exists()) {
				runId++;
				output = new File("output" + File.separator + "Particles_run"
						+ runId + "_step" + step + ".png");
			}

			BufferedImage img = new BufferedImage(1001, 101,
					BufferedImage.TYPE_BYTE_GRAY);
			try {
				img = ImageIO.read(new File("output" + File.separator
						+ "Particle_Base.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (Particle p : particles) {
				int position = (int) Math.floor(p.getPosition() * 100);
				for (int i = 0; i <= p.getWeight() * 100*ZOOM_FACTOR; i++) {
					img.setRGB(position, 100 - i, Color.BLACK.getRGB());
				}
			}

			try {
				ImageIO.write(img, "png", output);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
