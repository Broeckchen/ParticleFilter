
public class MainClass {

	public static void main(String[] args) {
		ParticleFilter pf = new ParticleFilter();
		pf.initialize(0.0, 10.0, 100);
		pf.saveVisualisation();
		pf.sensorStep(true);
		pf.saveVisualisation();
		pf.moveStep(2.0);
		pf.saveVisualisation();
		
		pf.initialize(0, 10, 7000);
		pf.saveVisualisation();
		pf.sensorStep(true);
		pf.saveVisualisation();
		
//		pf.initialize(0.0, 10.0, 100);
//		pf.saveVisualisation();
//		pf.sensorStep(true);
//		pf.saveVisualisation();
//		pf.moveStep(2.5);
//		pf.saveVisualisation();
//		pf.sensorStep(true);
//		pf.saveVisualisation();
//		pf.moveStep(-2.5);
//		pf.saveVisualisation();
//		pf.sensorStep(true);
//		pf.saveVisualisation();
//		pf.moveStep(4.5);
//		pf.saveVisualisation();
//		pf.sensorStep(true);
//		pf.saveVisualisation();
//		
//		pf.initialize(0.0, 10.0, 100);
//		pf.saveVisualisation();
//		pf.sensorStep(true);
//		pf.saveVisualisation();
//		
//		pf.initialize(0.0, 10.0, 100);
//		pf.saveVisualisation();
//		pf.sensorStep(true);
//		pf.saveVisualisation();	
	}
}