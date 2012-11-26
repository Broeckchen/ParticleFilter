import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.math3.distribution.NormalDistribution;

public class ParticleFilter {

	private static final double RELIABILITY = 0.9;
	private static final double STANDARD_DEVIATION = 0.5;
	private static final double MOVE_DEVIATION_FACTOR = 0.25;
	private static final NormalDistribution PRIOR = new NormalDistribution(0,
			STANDARD_DEVIATION);
	private static final double[] doors = { 1.0, 2.5, 5, 7 };

	private static final double ZOOM_FACTOR = 0.3;
	private static final int PIC_HEIGHT = 100;
	private static final double PIC_WIDTH_FACTOR = 10;

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

	public void sensorStep(boolean doorDetected) {
		step++;
		if (doorDetected) {
			double normalisationQuotient = 0.0;
			for (Particle p : particles) {
				double distance = findDoorDistance(p.getPosition());
				double prior = PRIOR.density(distance);
				double newWeight = p.getWeight()
						* (RELIABILITY * prior + (1 - RELIABILITY)
								* (1 - prior));
				p.setWeight(newWeight);
				normalisationQuotient += newWeight;
			}
			System.out.println(normalisationQuotient);
			for (Particle p : particles) {
				p.setWeight(p.getWeight() / normalisationQuotient);
			}
		} else {
			double normalisationQuotient = 0.0;
			for (Particle p : particles) {
				double distance = findDoorDistance(p.getPosition());
				double prior = PRIOR.density(distance);
				double newWeight = p.getWeight()
						* (RELIABILITY * (1 - prior) + (1 - RELIABILITY)
								* prior);
				p.setWeight(newWeight);
				normalisationQuotient += newWeight;
			}
			System.out.println(normalisationQuotient);
			for (Particle p : particles) {
				p.setWeight(p.getWeight() / normalisationQuotient);
			}
		}
	}

	public double findDoorDistance(double position) {
		double minDistance = rangeEnd;
		for (double doorPosition : doors) {
			double distance = Math.abs(doorPosition - position);
			if (distance < minDistance) {
				minDistance = distance;
			}
		}
		return minDistance;
	}

	// done using systematic resampling
	public void moveStep(double movement) {
		step++;
		NormalDistribution moveDistribution = new NormalDistribution(movement,
				Math.abs(MOVE_DEVIATION_FACTOR * movement));
		List<Particle> resampledParticles = new ArrayList<Particle>();
		Random r = new Random();
		while (resampledParticles.size() < amount) {
			int necessaryParticles = amount - resampledParticles.size();
			double startingPoint = r.nextDouble() / necessaryParticles;
			double cumulativeWeights = 0.0;
			double iterationParticleCount = 0.0;
			for (Particle p : particles) {
				double treshold = p.getWeight() + cumulativeWeights;
				while (iterationParticleCount / necessaryParticles
						+ startingPoint < treshold) {
					double newPosition = p.getPosition()
							+ moveDistribution.sample();
					if (newPosition <= rangeEnd && newPosition >= rangeStart) {
						resampledParticles.add(new Particle(newPosition,
								1.0 / amount));
					}
					iterationParticleCount++;
				}
				cumulativeWeights = treshold;
			}
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

			BufferedImage img = new BufferedImage((int) (PIC_WIDTH_FACTOR
					* amount + 1), PIC_HEIGHT + 1, BufferedImage.TYPE_BYTE_GRAY);
			for (int i = 0; i < PIC_HEIGHT + 1; i++) {
				for (int j = 0; j < PIC_WIDTH_FACTOR * amount + 1; j++) {
					img.setRGB(j, i, Color.WHITE.getRGB());
				}
			}
			for (Particle p : particles) {
				int position = (int) Math.floor(p.getPosition()
						* PIC_WIDTH_FACTOR * amount / (rangeEnd - rangeStart));
				for (int i = 0; i <= p.getWeight() * PIC_HEIGHT * ZOOM_FACTOR
						* amount; i++) {
					img.setRGB(position, PIC_HEIGHT - i, Color.BLACK.getRGB());
				}
			}

			FileWriter textOutput = null;
			try {
				textOutput = new FileWriter("output" + File.separator
						+ "Particles_run" + runId + "_step" + step + ".txt");
				for (Particle p : particles) {
					textOutput.write("position=" + p.getPosition() + "\tweight="
							+ p.getWeight() + "\r\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (textOutput != null) {
					try {
						textOutput.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
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
