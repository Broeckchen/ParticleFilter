import org.apache.commons.math3.distribution.NormalDistribution;

/*
 * This class is used for the discrete Bayes filter
 * implementation. We have implemented the sensor
 * and motion model for a robot that moves in a corridor.
 * We consider only one-dimension movement.
 */

public class DiscreteBayesFilter {
	
	public static final double RELIABILITY = 0.9;	// Our sensor is reliable by 90% for detecting a job
	public static final double STANDARD_DEVIATION = 0.5;	// The sensor model deviation
	public static final double MOVE_DEVIATION_FACTOR = 0.25;	// The motion model deviation
	
	private double[] sensorBelief;
	private double[] motionBelief;
	private NormalDistribution motionModelProbability;	// Motion model probability function
	private static final NormalDistribution sensorModelProbability = new NormalDistribution(0, STANDARD_DEVIATION); // Sensor model probability function
	
	public DiscreteBayesFilter(int cells) {

		this.sensorBelief = new double[cells];
		this.motionBelief = new double[cells];
		for(int i=0; i<sensorBelief.length; i++){
			sensorBelief[i]=0.0;
			motionBelief[i]=0.0;
		}
	}

	/*
	 * This function computes the discrete Bayes filter
	 * for sensor and motion model. As input we give the
	 * doors positions, the previous probability of the 
	 * robot position, a boolean flag if a door is detected
	 * we give true as input. Also we give as input if an
	 * action is taken place and the movement of the robot
	 * in meters. The move attribute is positive if robot moves
	 * to the right, negative otherwise.
	 */
	public double[] bayesFilter(int[] doors, double[] oldBelief, boolean doorFound, boolean action, double move){
		
		double h=0;
		int cells = sensorBelief.length;	
		double[] newBelief = new double[cells]; 
		double[] pzx = pZX(doors, cells);
		
		for(int q=0; q<newBelief.length; q++) // Initialize the new belief of the robot
			newBelief[q] = 0;

		if(!action){	// if no action is taken place
			if(doorFound){	// and a door is found we apply the sensor model
				for(int i=0; i<cells; i++){	// for computing the new belief of the robot we multiply the old belief
					// with the sensor model. Our sensor is reliable by 90% so we multiply the pzx with 90% and
					// the p(-z|x) probability with 0.1 in order to get the right probability of pzx.
					newBelief[i] = oldBelief[i]*(RELIABILITY*pzx[i] + (1-RELIABILITY)*(1-pzx[i]));
					h+= newBelief[i];	// update the normalization factor
				}
		
				for(int i=0; i<newBelief.length; i++)
					sensorBelief[i] = newBelief[i]/h;	// normalize the new belief and we get the sensor belief

				return sensorBelief;
			}else
				return null;	// no information for sensor model
		}
		else{	// if an action is taken place we apply the motion model
			// We compute the motion model regarding the MOVE_DEVIATION_FACTOR
			motionModelProbability = new NormalDistribution(0, MOVE_DEVIATION_FACTOR*move);
			motionBelief = new double[oldBelief.length]; 
			for(int i=0; i<oldBelief.length; i++){	// update the motion model belief
				motionBelief[i] = pXUXBelief(move, i, oldBelief);
			}
			return motionBelief;
		}
	}
	
	/*
	 * This function computes the probability of p(z|x) given
	 * that the sensor detected a door
	 */
	public double[] pZX(int doors[], int cells){
		double standardDeviation = 0.5;
		int deviationCells = (int)(standardDeviation*10);
		int door =0;
		int i=0, j=0;;
		double[] prob= new double[cells];
		for(i =0;  i<prob.length; i++)
			prob[i] =0.0;
		for(i =0;  i<prob.length; i++){
			if(door<doors.length && i==doors[door]){
				for(j=0;j<2*deviationCells+1; j++){	// update all the cells in distance 0.5 meters according to the normal distribution
						prob[i-deviationCells+j] = sensorModelProbability.density(Math.abs((j-deviationCells)*0.1));
				}
				door++;	// update for the next door
			}
		}
		return prob;
	}
	
	/*
	 * This function computes the sum of P(x|u,x') for a given x and a 
	 * move "movement" according to the previous belief of the robot
	 */
	public double pXUXBelief(double movement, int x, double[] belief){
		
		int standardDeviation = (int) (MOVE_DEVIATION_FACTOR*movement*10);
		int oldX = (int) (x-(movement*10));	// the x in the previous state
		double bel =0.0;
		int min = oldX-standardDeviation;
		int max = oldX+standardDeviation;
		
		if(max<0 ||min>belief.length ){
			return 0;
		}
		for(int i=0; i<belief.length; i++){
			if(i>=min && i<=max){	// we sum the probabilities for everu cell that
				// belongs to the space determined by the previous position and the 
				// deviation of the move. We apply a normal distribution 
				bel += motionModelProbability.density((oldX-i)*0.1)*belief[i];
			}
		}
		return bel;
	}
}
