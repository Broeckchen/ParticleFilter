import org.apache.commons.math3.distribution.NormalDistribution;

public class DiscreteBayesFilter {
	
	private double[] sensorBelief;
	private double[] motionBelief;
	private static final NormalDistribution probability = new NormalDistribution(0, 0.5);
	
	public DiscreteBayesFilter(int cells) {

		this.sensorBelief = new double[cells];
		this.motionBelief = new double[cells];
		for(int i=0; i<sensorBelief.length; i++){
			sensorBelief[i]=0.0;
			motionBelief[i]=0.0;
		}
	}

	
	public double[] bayesFilter(int[] doors, double[] oldBelief,boolean doorFound, boolean action, double move){
		
		double h=0;
		
		int cells = sensorBelief.length;
		
		double[] newBelief = new double[cells]; 
		for(int q=0; q<newBelief.length; q++)
			newBelief[q] = 0;
		double[] doorBelief = pZX(doors, cells);

//		System.out.println();
//		System.out.println(" Deviation Door Range "+ Integer.toString(deviationCells*2 +1));
//		System.out.println();
//		
		if(!action){
			//System.out.println("NEW belief");
			if(doorFound){
			
	//			System.out.println();
				for(int i=0; i<cells; i++){
					newBelief[i] = oldBelief[i]*(0.9*doorBelief[i] + 0.1*(1-doorBelief[i]));
					h+= newBelief[i];
				}
		
				for(int i=0; i<newBelief.length; i++){
					sensorBelief[i] = newBelief[i]/h;
	//				System.out.print(newBelief[i]+"\t");
				}
	//			System.out.println();
				return sensorBelief;
			}else
				return null;
		}
		else{
			motionBelief = new double[oldBelief.length]; 
			for(int i=0; i<oldBelief.length; i++){
				motionBelief[i] = pXUXBelief(move, i, oldBelief);
			}
//			h=0;
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			for(int i=0; i<belief.length; i++){
//				h+= belief[i];
//				System.out.print(belief[i]+"\t");
//			}
//			System.out.println();
//			System.out.println();
//			for(int i=0; i<belief.length; i++){
//				belief[i] = belief[i]/h;
//				System.out.print(belief[i]+"\t");
//			}
//			System.out.println();
//			System.out.println();
//			System.out.println();
			return motionBelief;
		}
	}
	
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
				for(j=0;j<2*deviationCells+1; j++){
						prob[i-deviationCells+j] =this.probability.density(Math.abs((j-deviationCells)*0.1));
				}
				door++;
			}
		}
		return prob;
	}
	
	public double pXUXBelief(double movement, int x, double[] belief){
		
//		System.out.println();
//
//		System.out.println("To belief pou pairnw");
//		for(int i=0; i<belief.length; i++)
//		System.out.print(belief[i]+"\t");
//
//		System.out.println();
//
//		System.out.println();
//		
//		System.out.println();
		
		int standardDeviation = (int) (2.5*movement);
		int oldX = (int) (x-(movement*10));
		double bel =0.0;
		int min = oldX-standardDeviation;
		int max = oldX+standardDeviation;
		if(max<0 ||min>belief.length ){
		//	System.out.println("Ektos oriwn");
			return 0;
		}
		//System.out.println();
		//System.out.println("Gia x "+x +" kai palio X " +oldX);
		for(int i=0; i<belief.length; i++){

			if(i>=min && i<=max){
				
				bel +=this.probability.density((oldX-i)*0.1)*belief[i];
//				System.out.println("upologizw gia i "+i+ " belief ");
//				System.out.println(Double.toString((Math.pow(Math.sqrt(2*Math.PI)*0.5 ,-1)*Math.exp(-Math.pow(oldX-i,2)*2))*belief[i]));

			}
		}
		
		//System.out.println("Belief " +Double.toString(bel));
		return bel;
	}
}
