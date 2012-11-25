import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.lowagie.text.Font;


public class MainClass {

	public static void main(String[] args) {
		
		boolean particleFilter = false;
		if(particleFilter){
			ParticleFilter pf = new ParticleFilter();
			pf.initialize(0.0, 10.0, 100);
			pf.saveVisualisation();
			pf.sensorStep(true);
			pf.saveVisualisation();
			pf.moveStep(2.0);
			pf.saveVisualisation();
	//		
	//		pf.initialize(0, 10, 7000);
	//		pf.saveVisualisation();
	//		pf.sensorStep(true);
	//		pf.saveVisualisation();
			
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
			}
		else{
			double[] corridor = new double[100];
			for(int i=0; i<corridor.length; i++){
				corridor[i] = 0.01;
				System.out.print(corridor[i]+"\t");
			}
			System.out.println();
			
			int[] doors = new int[4];
			doors[0] = 10;
			doors[1] = 25;
			doors[2] = 50;
			doors[3] = 70;
			
			DiscreteBayesFilter filter = new DiscreteBayesFilter(corridor.length); 
			double[] belief = filter.bayesFilter(doors, corridor, true, false, 0);
			
			double[] motionBelief = filter.bayesFilter(doors, belief,false, true, 2);
			XYSeries series1 = new XYSeries("Discrete Bayes Filter");
			System.out.println();
			System.out.println("BELIEF");
			for(int i=0; i<belief.length; i++){
				//series1.add(i*0.1, belief[i]);
				for(int j=0; j<10; j++)
					series1.add(i*0.1+j*0.01, belief[i]);
				System.out.println(belief[i]);
			}
			XYDataset dataSet1 = new XYSeriesCollection(series1);
		
			System.out.println();
			System.out.println("Cells "+motionBelief.length);
			
			XYSeries series2 = new XYSeries("Discrete Bayes Filter");
			
			for(int i=0; i<motionBelief.length; i++){
				//	series2.add(i*0.1, motionBelief[i]);
			for(int j=0; j<10; j++)
					series2.add(i*0.1+j*0.01, motionBelief[i]);
				System.out.print(motionBelief[i]+"\t");
			}
			XYDataset dataSet2 = new XYSeriesCollection(series2);
			JFreeChart chart = plotVector(dataSet1, dataSet2);
				
			ChartPanel panel = new ChartPanel(chart, true, true, true, false, true);
				 
			panel.setPreferredSize(new java.awt.Dimension(800, 500));
				 
			ApplicationFrame  frame = new ApplicationFrame("Bayes Discrete Filter");
				 	 
			frame.setContentPane(panel);
				 		
			frame.pack();
				 	 
			RefineryUtilities.centerFrameOnScreen(frame);
				 	 
			frame.setVisible(true);
			
		}
	}
	
	public  static JFreeChart plotVector(XYDataset dataSet1,XYDataset dataSet2 ){
	 	 
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
	 	 
		final NumberAxis rangeAxis1 = new NumberAxis("Belief");
		
		final XYPlot subplot1 = new XYPlot(dataSet1, null, rangeAxis1, renderer1);
		
		subplot1.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		
		java.awt.Font font =new java.awt.Font("SansSerif", Font.NORMAL, 9);
		
		final XYTextAnnotation annotation = new XYTextAnnotation("Hello!", 50.0, 10000.0);
		
		annotation.setFont(font);
		
		annotation.setRotationAngle(Math.PI / 4.0);
		
		subplot1.addAnnotation(annotation);
		final XYItemRenderer renderer2 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis2 = new NumberAxis("Belief");
		rangeAxis2.setAxisLineVisible(true);
		final XYPlot subplot2 = new XYPlot(dataSet2, null, rangeAxis2, renderer2);
		
		subplot2.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);	
		subplot2.addAnnotation(annotation);
		      
		
		// parent plot...
		
		final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis("Corridor Cells"));
		
		plot.setGap(10.0);
		
		
		
		// add the subplots...
		
		plot.add(subplot1, 1);
		plot.add(subplot2, 2);
		
		plot.setOrientation(PlotOrientation.VERTICAL);
		
		
		// return a new chart containing the overlaid plot...
		
		return new JFreeChart("Probability Density Function",
		
		                      JFreeChart.DEFAULT_TITLE_FONT, plot, true);

	
	 	 	
	  }
}