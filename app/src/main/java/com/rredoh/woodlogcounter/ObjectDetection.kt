package com.rredoh.woodlogcounter

import android.R.attr
import android.graphics.Bitmap
import android.R.attr.bitmap
import org.opencv.android.Utils
import org.opencv.core.*

import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.circle
import org.opencv.imgproc.Imgproc.rectangle
import kotlin.math.roundToInt


class ObjectDetection {
    fun detectCircle(bitmap: Bitmap): Pair<Bitmap,Int> {
        val mat = Mat(
            bitmap.width, bitmap.height,
            CvType.CV_8UC1
        )

        val grayMat = Mat(
            bitmap.width, bitmap.height,
            CvType.CV_8UC1
        )

        Utils.bitmapToMat(bitmap, mat)

        val colorChannels =
            if (mat.channels() == 3) Imgproc.COLOR_BGR2GRAY else if (mat.channels() == 4) Imgproc.COLOR_BGRA2GRAY else 1

        Imgproc.cvtColor(mat, grayMat, colorChannels)

//        Imgproc.blur(grayMat, grayMat, Size(7.0, 7.0), Point(2.0, 2.0))

//        Imgproc.GaussianBlur(
//            grayMat,
//            grayMat,
//            Size(9.0, 9.0), 2.0)

        val grayMatTemp = Mat(
            bitmap.width, bitmap.height,
            CvType.CV_8UC1
        )
        grayMat.copyTo(grayMatTemp)

        Imgproc.bilateralFilter(
            grayMatTemp,
            grayMat,
            0,
            20.0,
            10.0
        )

//        Imgproc.equalizeHist(grayMat, grayMat);
        val clahe = Imgproc.createCLAHE(2.0, Size(8.0, 8.0))
        clahe.apply(grayMat, grayMat)

//        val low_val = Scalar(0.0,17.0,50.0)
//        val high_val = Scalar(59.0,185.0,218.0)
//        Core.inRange(grayMat, low_val, high_val, grayMat)
//
//        Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_GRAY2BGR)

//        Utils.matToBitmap(grayMat, bitmap)
//        return Pair(bitmap, 0)


// accumulator value

// accumulator value
        val dp = 1.2
// minimum distance between the center coordinates of detected circles in pixels
// minimum distance between the center coordinates of detected circles in pixels
        val minDist = 10.0

// min and max radii (set these values as you desire)

// min and max radii (set these values as you desire)
        val minRadius = 5
        val maxRadius = 100

// param1 = gradient value used to handle edge detection
// param2 = Accumulator threshold value for the
// cv2.CV_HOUGH_GRADIENT method.
// The smaller the threshold is, the more circles will be
// detected (including false circles).
// The larger the threshold is, the more circles will
// potentially be returned.

// param1 = gradient value used to handle edge detection
// param2 = Accumulator threshold value for the
// cv2.CV_HOUGH_GRADIENT method.
// The smaller the threshold is, the more circles will be
// detected (including false circles).
// The larger the threshold is, the more circles will
// potentially be returned.
        val param1 = 70.0
        val param2 = 72.0

/* create a Mat object to store the circles detected */

/* create a Mat object to store the circles detected */
        val circles = Mat(
            bitmap.width,
            bitmap.height, CvType.CV_8UC1
        )

/* find the circle in the image */

/* find the circle in the image */
        Imgproc.HoughCircles(
            grayMat, circles,
            Imgproc.CV_HOUGH_GRADIENT, dp, minDist, param1,
            param2, minRadius, maxRadius
        )
//        Imgproc.HoughCircles(grayMat, circles,
//            Imgproc.CV_HOUGH_GRADIENT,
//            2.0,
//            100.0,
//            100.0,
//            90.0,
//            0,
//            1000)

/* get the number of circles detected */

/* get the number of circles detected */
        val numberOfCircles = if (circles.rows() == 0) 0 else circles.cols()

/* draw the circles found on the image */

/* draw the circles found on the image */
        for (i in 0 until numberOfCircles) {


/* get the circle details, circleCoordinates[0, 1, 2] = (x,y,r)
 * (x,y) are the coordinates of the circle's center
 */
            val circleCoordinates = circles[0, i]
            val x = circleCoordinates[0]
            val y = circleCoordinates[1]
            val center = Point(x, y)
            val radius = circleCoordinates[2]

            /* circle's outline */
            circle(
                mat, center, radius.roundToInt(), Scalar(
                    0.0,
                    255.0, 0.0
                ), 4
            )

            /* circle's center outline */
            rectangle(
                mat, Point(x - 5, y - 5),
                Point(x + 5, y + 5),
                Scalar(0.0, 128.0, 255.0), -1
            )
        }

/* convert back to bitmap */

/* convert back to bitmap */
        Utils.matToBitmap(mat, bitmap)

        return Pair(bitmap, numberOfCircles)
    }
}