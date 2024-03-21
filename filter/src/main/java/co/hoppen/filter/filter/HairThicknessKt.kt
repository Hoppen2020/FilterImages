package co.hoppen.filter.filter

import android.graphics.Bitmap
import co.hoppen.filter.FilterInfoResult
import com.blankj.utilcode.util.LogUtils
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.Random

/**
 * Created by YangJianHui on 2024/3/20.
 */
class HairThicknessKt :Filter() {
    override fun onFilter(filterInfoResult: FilterInfoResult) {
        val width = originalImage.width
        val height = originalImage.height

        val orMat = Mat()
        Utils.bitmapToMat(originalImage,orMat)

        val grayMat = Mat()
        val sourceMat = Mat()
        val binaryMat = Mat()

        Imgproc.cvtColor(orMat,sourceMat, Imgproc.COLOR_RGBA2RGB)
        Imgproc.cvtColor(orMat,grayMat, Imgproc.COLOR_RGB2GRAY)
        Imgproc.GaussianBlur(grayMat,grayMat, Size(5.0, 5.0),2.0,2.0)
        Imgproc.threshold(grayMat,binaryMat,10.0,255.0, Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU)//20.0

        val hsv = Mat()
        Imgproc.cvtColor(sourceMat,hsv, Imgproc.COLOR_RGB2HSV)
        val mask = Mat()
        Core.inRange(hsv, Scalar(0.0, 0.0, 200.0), Scalar(180.0, 255.0, 255.0), mask)
        Core.subtract(binaryMat,mask,binaryMat)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(binaryMat,contours,hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        val tmp = sourceMat.clone()

        for (i in 0 until contours.size){
            LogUtils.e(contours[i].size().toString(),contours[i].size().area())
            if (contours[i].size().area()<=50.0){
                continue
            }
            val result =  MatOfPoint2f()
            val source = MatOfPoint2f()
            source.fromList(contours[i].toList())
            Imgproc.approxPolyDP(source,result,4.0,true)
            val points = result.toArray()

            val rect = Imgproc.minAreaRect(source)

            Imgproc.putText(tmp,"${contours[i].size().area().toInt()}",rect.center,
                3,1.0,Scalar(255.0,255.0,0.0),2)

            for (j in points.indices){
                Imgproc.line(tmp,
                    points[j % points.size],
                    points[(j+1)%points.size],
                    Scalar(255.0,255.0,0.0),
                    2,
                    Imgproc.LINE_8)
            }

        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(tmp,bitmap)

        orMat.release()
        grayMat.release()
        binaryMat.release()
        sourceMat.release()
        hsv.release()
        mask.release()
        hierarchy.release()
        tmp.release()

        filterInfoResult.score = Random().nextInt(16) + 48
        filterInfoResult.setHairFilterBitmap(bitmap)
        filterInfoResult.status = FilterInfoResult.Status.SUCCESS
    }

}