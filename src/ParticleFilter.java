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

	// parameters for the sensor and motion model
	private static final double RELIABILITY = 0.9;
	private static final double STANDARD_DEVIATION = 0.5;
	private static final double MOVE_DEVIATION_FACTOR = 0.25;
	private static final NormalDistribution PRIOR = new NormalDistribution(0,
			STANDARD_DEVIATION);
	private static final double[] doors = { 1.0, 2.5, 5, 7 };

	// parameters for the visualisation
	private static final double ZOOM_FACTOR = 0.3;
	private static final int PIC_HEIGHT = 100;
	private static final double PIC_WIDTH_FACTOR = 10;

	// member variables
	private int step;
	private List<Particle> particles;
	private int amount;
	private double rangeStart;
	private double rangeEnd;

	public ParticleFilter() {
		this.particles = new ArrayList<Particle>();
		step = -1;
	}

	/*
	 * Initializes the Particle Filter with amount particles uniformly
	 * distributed between rangeStart and rangeEnd with equal weight of
	 * 1/amount.
	 * 
	 * In case of the assignment, rangeStart=0, rangeEnd=10 and amount=100.
	 */
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

	/*
	 * This function reweights all particles according to the sensor model.
	 * 
	 * The function used to reweight the particles is based on the distance x to
	 * the closest door: 0.9*p(z|x) + 0.1*p(-z|x), using p(-z|x) = 1-p(z|x),
	 * while p(z|x) is normally distributed.
	 * 
	 * After all particles have been reweighted according to that function, the
	 * weights of all particles are normalized to summ up to 1.
	 */
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
			for (Particle p : particles) {
				p.setWeight(p.getWeight() / normalisationQuotient);
			}
		} else {
			// not needed for the assignment
		}
	}

	/*
	 * This function calculates the distance to the closest door.
	 */
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

	/*
	 * This function resamples the particles according to the motion model and
	 * the distribution given by the weights of the particles.
	 * 
	 * Using systematic resampling amount (100 in the assignment) particles are
	 * chosen according to their weight (particles with high weights might be
	 * chosen multiple times, particles with low weight on the other hand
	 * never). Then those particles are moved and disturbed according to the
	 * motion command. The resulting particles are uniformly weighted and make
	 * up the new particle set.
	 * 
	 * If a particles moves out of the given range (0 - 10 in the assignment) it
	 * is discarded. If at the end of the resampling there aren't enough
	 * particles, the missing number of particles is resampled again from the
	 * complete distribution. This is repeated until there are exactly as many
	 * particles as before.
	 */
	public void moveStep(double motion) {
		step++;
		NormalDistribution moveDistribution = new NormalDistribution(motion,
				Math.abs(MOVE_DEVIATION_FACTOR * motion));
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

	/*
	 * This function creates two files showing the current particles and their
	 * weights. One is a picture with a visualization of the particles as
	 * vertical black lines with a length according to their weight and a width
	 * of exactly 1 pixel, the other is a text file containing weight and
	 * position of all particles.
	 * 
	 * Note, that the size and scaling of the picture depends on the number of
	 * particles and some parameters given at the beginning of this file. Note
	 * that some particle lines might not fit completely into the picture if the
	 * parameters are chosen poorly. It might also occur, that some particles
	 * are not visible, if they are so close to another particle, that they fall
	 * into the same pixel line.
	 * 
	 * The files are automatically named to not overwrite previous files.
	 */

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

			// creation of an empty white picture of the correct bounds
			BufferedImage img = new BufferedImage((int) (PIC_WIDTH_FACTOR
					* amount + 1), PIC_HEIGHT + 1, BufferedImage.TYPE_BYTE_GRAY);
			for (int i = 0; i < PIC_HEIGHT + 1; i++) {
				for (int j = 0; j < PIC_WIDTH_FACTOR * amount + 1; j++) {
					img.setRGB(j, i, Color.WHITE.getRGB());
				}
			}

			// drawing the black line for each particles
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
					textOutput.write("position=" + p.getPosition()
							+ "\tweight=" + p.getWeight() + "\r\n");
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
