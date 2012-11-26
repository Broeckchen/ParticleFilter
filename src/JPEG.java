
import java.awt.*; 
import java.awt.geom.*; 
import java.awt.image.*; 
import java.io.*; 

import org.jfree.chart.JFreeChart;
import com.sun.image.codec.jpeg.*; 

public class JPEG {
	
	/*
	 * This class saves a chart as a JPEG file
	 * 
	 */
	public static void saveToFile(JFreeChart chart, String aFileName, 
			int width,int height, double quality) throws FileNotFoundException, IOException 					{ 
		
		BufferedImage img = draw( chart, width, height ); 

		FileOutputStream fos = new FileOutputStream(aFileName); 
		JPEGImageEncoder encoder2 = 
				JPEGCodec.createJPEGEncoder(fos); 
		JPEGEncodeParam param2 = 
				encoder2.getDefaultJPEGEncodeParam(img); 
		param2.setQuality((float) quality, true); 
		encoder2.encode(img,param2); 
		fos.close(); 
					} // saveToFile 

	protected static BufferedImage draw(JFreeChart chart, int width, int height) { 
		BufferedImage img = 
				new BufferedImage(width , height, 
						BufferedImage.TYPE_INT_RGB); 
		Graphics2D g2 = img.createGraphics(); 


		chart.draw(g2, new Rectangle2D.Double(0, 0, width, height)); 

		g2.dispose(); 
		return img; 
	} // end draw 
} // end class JPEG
