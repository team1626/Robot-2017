package org.usfirst.frc.team1626.robot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.opencv.core.*;
import org.opencv.core.Core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.vision.VisionPipeline;

/**
* Pipeline
*
* @author GRIP Pipeline Generator, also code by GitHub user @jcbakerglm
*/
public class Pipeline implements VisionPipeline {

	private Size newSizeOutput = new Size();
	private Mat cvGaussianblurOutput = new Mat();
	private Mat cvErodeOutput = new Mat();
	private Mat hsvThresholdOutput = new Mat();
	private ArrayList<MatOfPoint> findContoursOutput = new ArrayList<MatOfPoint>();
	
	public static final double SOURCE_HEIGHT = 640.0;
	public static final double SOURCE_WIDTH = 480.0;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	/**
	 * This is the primary method that runs the entire pipeline and updates the outputs.
	 */
	public void process(Mat source0) {
		NetworkTable.setIPAddress("localhost");
	    NetworkTable.setClientMode();
	    NetworkTable table = NetworkTable.getTable("/GRIP");
		
		// Step New_Size0:
		double newSizeWidth = -1.0;
		double newSizeHeight = -1.0;
		newSize(newSizeWidth, newSizeHeight, newSizeOutput);

		// Step CV_GaussianBlur0:
		Mat cvGaussianblurSrc = source0;
		Size cvGaussianblurKsize = newSizeOutput;
		double cvGaussianblurSigmax = 1.0;
		double cvGaussianblurSigmay = 7.0;
		int cvGaussianblurBordertype = Core.BORDER_DEFAULT;
		cvGaussianblur(cvGaussianblurSrc, cvGaussianblurKsize, cvGaussianblurSigmax, cvGaussianblurSigmay, cvGaussianblurBordertype, cvGaussianblurOutput);

		// Step CV_erode0:
		Mat cvErodeSrc = cvGaussianblurOutput;
		Mat cvErodeKernel = new Mat();
		Point cvErodeAnchor = new Point(-1, -1);
		double cvErodeIterations = 3.0;
		int cvErodeBordertype = Core.BORDER_CONSTANT;
		Scalar cvErodeBordervalue = new Scalar(-1);
		cvErode(cvErodeSrc, cvErodeKernel, cvErodeAnchor, cvErodeIterations, cvErodeBordertype, cvErodeBordervalue, cvErodeOutput);

		// Step HSV_Threshold0:
		Mat hsvThresholdInput = cvErodeOutput;
		
        // Values for reflective tape given an image
		double[] hsvThresholdHue = {89.02877697841726, 129.3174061433447};
		double[] hsvThresholdSaturation = {178.86690647482015, 255.0};
		double[] hsvThresholdValue = {59.62230215827338, 255.0};
        
		hsvThreshold(hsvThresholdInput, hsvThresholdHue, hsvThresholdSaturation, hsvThresholdValue, hsvThresholdOutput);

		// Step Find_Contours0:
		Mat findContoursInput = hsvThresholdOutput;
		boolean findContoursExternalOnly = true;
		findContours(findContoursInput, findContoursExternalOnly, findContoursOutput);
		
		int contourCount = findContoursOutput.size();
		Rect[] rect = new Rect[contourCount];

		int rectCount = 0;
		
		 // Create bounding rectangle for each contour
        for (int i = 0; i < contourCount; i++) {
            MatOfPoint points = new MatOfPoint(findContoursOutput.get(i));
            //rect[i] = Imgproc.boundingRect(points);
            
            Rect tempRect = Imgproc.boundingRect(points);
        }
        
        // Calculate the number of pair combinations
        int numOfPairs = ((rectCount - 1) * rectCount) / 2;
       
        TargetCandidate[] rectCandidates = new TargetCandidate[numOfPairs];
        
        int scoreIndex = 0; 

        // Score each pair combination
        for (int i = 0; i < (rectCount - 1); i++) {
            for (int j = i+1; j < rectCount; j++) {
                rectCandidates[scoreIndex] = new TargetCandidate(rect[i], rect[j], i, j);
                scoreIndex++;
            }
        }
        
        int highestScore = 250; 
        int bestPairIndex = -1;
        int targetIndex1 = -1;
        int targetIndex2 = -1;
        
        for (int i = 0; i < numOfPairs; i++) {
            int tempScore = rectCandidates[i].getTotalScore();
            
            if (tempScore > highestScore) {
                highestScore = tempScore; 
                bestPairIndex = i;
                targetIndex1 = rectCandidates[i].getRectL();
                targetIndex2 = rectCandidates[i].getRectR();
            }
        }
        
        double frameCenterX = (SOURCE_WIDTH / 2.0);
        double leftRectRightX = rect[targetIndex1].x + rect[targetIndex1].width;
        double rightRectLeftX = rect[targetIndex2].x;

        // These are the values to show in the HUD and to send to NetworkTable
        double targetCenterX = ( (rightRectLeftX - leftRectRightX) / 2.0) + leftRectRightX;
        double targetOffset = Math.abs(frameCenterX - targetCenterX);
        double distance = estimatedDistance(rect[targetIndex1].height, rect[targetIndex2].height);
        double heightRatioLvR = (double) (rect[targetIndex1].height) / (double) (rect[targetIndex2].height);
        double heightRatioRvL = (double) (rect[targetIndex2].height) / (double) (rect[targetIndex1].height);

        WriteToNetworkTable(table, targetCenterX, targetOffset, heightRatioLvR, heightRatioRvL, distance);
	}

	/**
	 * This method is a generated getter for the output of a New_Size.
	 * @return Size output from New_Size.
	 */
	public Size newSizeOutput() {
		return newSizeOutput;
	}

	/**
	 * This method is a generated getter for the output of a CV_GaussianBlur.
	 * @return Mat output from CV_GaussianBlur.
	 */
	public Mat cvGaussianblurOutput() {
		return cvGaussianblurOutput;
	}

	/**
	 * This method is a generated getter for the output of a CV_erode.
	 * @return Mat output from CV_erode.
	 */
	public Mat cvErodeOutput() {
		return cvErodeOutput;
	}

	/**
	 * This method is a generated getter for the output of a HSV_Threshold.
	 * @return Mat output from HSV_Threshold.
	 */
	public Mat hsvThresholdOutput() {
		return hsvThresholdOutput;
	}

	/**
	 * This method is a generated getter for the output of a Find_Contours.
	 * @return ArrayList<MatOfPoint> output from Find_Contours.
	 */
	public ArrayList<MatOfPoint> findContoursOutput() {
		return findContoursOutput;
	}


	/**
	 * Fills a size with given width and height.
	 * @param width the width of the size
	 * @param height the height of the size
	 * @param size the size to fill
	 */
	private void newSize(double width, double height, Size size) {
		size.height = height;
		size.width = width;
	}

	/**
	 * Performs a Gaussian blur on the image.
	 * @param src the image to blur.
	 * @param kSize the kernel size.
	 * @param sigmaX the deviation in X for the Gaussian blur.
	 * @param sigmaY the deviation in Y for the Gaussian blur.
	 * @param borderType pixel extrapolation method.
	 * @param dst the output image.
	 */
	private void cvGaussianblur(Mat src, Size kSize, double sigmaX, double sigmaY,
		int	borderType, Mat dst) {
		if (kSize == null) {
			kSize = new Size(1,1);
		}
		Imgproc.GaussianBlur(src, dst, kSize, sigmaX, sigmaY, borderType);
	}

	/**
	 * Expands area of lower value in an image.
	 * @param src the Image to erode.
	 * @param kernel the kernel for erosion.
	 * @param anchor the center of the kernel.
	 * @param iterations the number of times to perform the erosion.
	 * @param borderType pixel extrapolation method.
	 * @param borderValue value to be used for a constant border.
	 * @param dst Output Image.
	 */
	private void cvErode(Mat src, Mat kernel, Point anchor, double iterations,
		int borderType, Scalar borderValue, Mat dst) {
		if (kernel == null) {
			kernel = new Mat();
		}
		if (anchor == null) {
			anchor = new Point(-1,-1);
		}
		if (borderValue == null) {
			borderValue = new Scalar(-1);
		}
		Imgproc.erode(src, dst, kernel, anchor, (int)iterations, borderType, borderValue);
	}

	/**
	 * Segment an image based on hue, saturation, and value ranges.
	 *
	 * @param input The image on which to perform the HSL threshold.
	 * @param hue The min and max hue
	 * @param sat The min and max saturation
	 * @param val The min and max value
	 * @param output The image in which to store the output.
	 */
	private void hsvThreshold(Mat input, double[] hue, double[] sat, double[] val,
	    Mat out) {
		Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2HSV);
		Core.inRange(out, new Scalar(hue[0], sat[0], val[0]),
			new Scalar(hue[1], sat[1], val[1]), out);
	}

	/**
	 * Sets the values of pixels in a binary image to their distance to the nearest black pixel.
	 * @param input The image on which to perform the Distance Transform.
	 * @param type The Transform.
	 * @param maskSize the size of the mask.
	 * @param output The image in which to store the output.
	 */
	private void findContours(Mat input, boolean externalOnly,
		List<MatOfPoint> contours) {
		Mat hierarchy = new Mat();
		contours.clear();
		int mode;
		if (externalOnly) {
			mode = Imgproc.RETR_EXTERNAL;
		}
		else {
			mode = Imgproc.RETR_LIST;
		}
		int method = Imgproc.CHAIN_APPROX_SIMPLE;
		Imgproc.findContours(input, contours, hierarchy, mode, method);
	}
	
	private double estimatedDistance(int heightL, int heightR){
        int averageHeight = (heightL + heightR) / 2;
        double distance = 0.0;

        if (averageHeight < 60) {
            distance = (0.0000555453*Math.pow(averageHeight,3)) - (0.00290273*Math.pow(averageHeight,2)) - (0.219377*averageHeight) + 15.6542;
        } else {
            distance = (-0.00000710617*Math.pow(averageHeight,3)) + (0.00212286*Math.pow(averageHeight,2)) - (0.239151*averageHeight) + 12.2385;
        }

        return distance;
    }
    
    private void WriteToNetworkTable(NetworkTable table, double targetCenterX, double targetOffset, double heightRatioLvR, double heightRatioRvL, double distance) {
        table.putNumber("targetCenterX", targetCenterX);
        table.putNumber("targetOffset", targetOffset);
        table.putNumber("distance", distance);
        table.putNumber("heightRatioLvR", heightRatioLvR);
        table.putNumber("heightRatioRvL", heightRatioRvL);
    }
}