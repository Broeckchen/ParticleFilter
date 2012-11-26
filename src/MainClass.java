import java.awt.Toolkit;
import java.io.*;

import org.jfree.chart.*;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
import org.jfree.ui.ApplicationFrame;

import com.lowagie.text.Font;

public class MainClass {

	public static void main(String[] args) {

		MainClass main = new MainClass();

		// Run Particle Filter
		ParticleFilter pf = new ParticleFilter();
		pf.initialize(0.0, 10.0, 100);
		pf.saveVisualisation();
		pf.sensorStep(true);
		pf.saveVisualisation();
		pf.moveStep(2.0);
		pf.saveVisualisation();

		// Run Bayes Filter
		double[] corridor = new double[100];

		for (int i = 0; i < corridor.length; i++)
			corridor[i] = 0.01;

		int[] doors = new int[4];
		doors[0] = (int) (1.0 * 10);
		doors[1] = (int) (2.5 * 10);
		doors[2] = (int) (5.0 * 10);
		doors[3] = (int) (7.0 * 10);

		DiscreteBayesFilter filter = new DiscreteBayesFilter(corridor.length);
		double[] belief = filter.bayesFilter(doors, corridor, true, false, 0);

		double[] motionBelief = filter.bayesFilter(doors, belief, false, true,
				2);

		// Create the chart of sensor model probability and
		// motion model probability

		XYSeries series1 = new XYSeries("Discrete Bayes Filter");

		for (int i = 0; i < belief.length; i++) {
			for (int j = 0; j < 10; j++)
				series1.add(i * 0.1 + j * 0.01, belief[i]); // we want to
															// present the
															// probability as
		} // as probability of cells

		XYDataset dataSet1 = new XYSeriesCollection(series1);

		XYSeries series2 = new XYSeries("Discrete Bayes Filter");

		for (int i = 0; i < motionBelief.length; i++) {
			for (int j = 0; j < 10; j++)
				series2.add(i * 0.1 + j * 0.01, motionBelief[i]);
		}
		XYDataset dataSet2 = new XYSeriesCollection(series2);

		JFreeChart chart1 = main.plotVector(dataSet1);
		JFreeChart chart2 = main.plotVector(dataSet2);

		try {
			JPEG.saveToFile(chart1, "output" + File.separator
					+ "sensorModelBayesFilter.jpeg", 800, 500, 0.9);
			JPEG.saveToFile(chart2, "output" + File.separator
					+ "motionModelBayesFilter.jpeg", 800, 500, 0.9);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int width = 500;
		int height = 300;

		ChartPanel panel1 = new ChartPanel(chart1, true, true, true, false,
				true);

		panel1.setPreferredSize(new java.awt.Dimension(width, height));

		ApplicationFrame frame1 = new ApplicationFrame(
				"Bayes Discrete Filter Sensor Model");

		frame1.setContentPane(panel1);

		frame1.pack();

		frame1.setLocation(
				(Toolkit.getDefaultToolkit().getScreenSize().width - width) / 5,
				(Toolkit.getDefaultToolkit().getScreenSize().height - height) / 5);

		frame1.setVisible(true);

		ChartPanel panel2 = new ChartPanel(chart2, true, true, true, false,
				true);

		panel2.setPreferredSize(new java.awt.Dimension(width, height));

		ApplicationFrame frame2 = new ApplicationFrame(
				"Bayes Discrete Filter Motion Model");

		frame2.setContentPane(panel2);

		frame2.pack();

		frame2.setLocation(frame1.getLocation().x + 5 * width / 4,
				frame1.getLocation().y);

		frame2.setVisible(true);

	}

	/*
	 * This function generates the plot of the given data set.
	 */
	public JFreeChart plotVector(XYDataset dataSet1) {

		XYItemRenderer renderer1 = new StandardXYItemRenderer();

		NumberAxis rangeAxis1 = new NumberAxis("Belief");

		XYPlot plot1 = new XYPlot(dataSet1, null, rangeAxis1, renderer1);

		plot1.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

		java.awt.Font font = new java.awt.Font("SansSerif", Font.NORMAL, 9);

		XYTextAnnotation annotation = new XYTextAnnotation("Hello!", 50.0,
				10000.0);

		annotation.setFont(font);

		annotation.setRotationAngle(Math.PI / 4.0);

		CombinedDomainXYPlot plot = new CombinedDomainXYPlot();
		plot.addAnnotation(annotation);
		plot.add(plot1);
		plot.setOrientation(PlotOrientation.VERTICAL);

		return new JFreeChart("Probability Density Function",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}
}