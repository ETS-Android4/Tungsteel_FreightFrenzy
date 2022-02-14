
package org.firstinspires.ftc.teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Libraries.AprilTagDetectionPipeline;
import org.firstinspires.ftc.teamcode.Libraries.Subsystem;
import org.opencv.core.Point;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.apriltag.AprilTagPose;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;

import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.annotation.CheckForNull;

public class CameraHardwareAprilTag extends Subsystem {
    OpenCvCamera camera;
    AprilTagDetectionPipeline aprilTagDetectionPipeline;
    int framesWithoutDetection = 0;
    /* ***** CONFIGURATION ***** */
    double tagsize = 0.0508; //Meters
    // Lens intrinsics  // CONFIGURED WITH ZEPHYR BY KYLE
    double fx = 1038.02; //
    double fy = 1038.02; //
    double cx = 630.346; //
    double cy = 497.208; //
    //                   //
    /* ************************* */
    public CameraHardwareAprilTag(HardwareMap hwMap){
        // This chunk of code sets up the camera, and feeds the images captured from the camera to the EOCV pipeline
        int cameraMonitorViewId = hwMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hwMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        aprilTagDetectionPipeline = new AprilTagDetectionPipeline(tagsize, fx, fy, cx, cy);
        aprilTagDetectionPipeline.setDecimation(3);
        camera.setPipeline(aprilTagDetectionPipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(1280, 960, OpenCvCameraRotation.UPRIGHT);
            }
            @Override
            public void onError(int errorCode) {
            }
        });
    }
    public AprilTagDetection[] getDetections() { @CheckForNull //possibly returns null, be careful of that
    ArrayList<AprilTagDetection> detectionsList = aprilTagDetectionPipeline.getLatestDetections();
        if(detectionsList == null) return null; //exit the method and return null if the camera has not returned a frame yet
        AprilTagDetection[] detections = new AprilTagDetection[detectionsList.size()];
        detections = detectionsList.toArray(detections); // convert the ArrayList to an Array

        if (detections.length == 0) framesWithoutDetection++;
        else framesWithoutDetection = 0;
        if (framesWithoutDetection >= 6) //6 is adjustable, after 6 frames without detecting anything, lower threshold for detecting tags
            aprilTagDetectionPipeline.setDecimation(3);
        return detections;
    }
    public boolean newDetectionExists(){
        ArrayList<AprilTagDetection> detectionsList = new ArrayList<AprilTagDetection>();
        detectionsList = aprilTagDetectionPipeline.getDetectionsUpdate();
        if(detectionsList != null){
            return detectionsList.size() > 0;
        }
        return false;
    }
    public AprilTagDetection getSingleDetection(){
        if(getDetections().length != 0) return getDetections()[0];
        return null;
    }
    public Integer whichDuck(){
        int FIRST = -100; //50, -90, -110
        int SECOND = -80;
        int THIRD = 0;
        if(getDetections() == null || getDetections().length == 0) return 0;
        double center = getSingleDetection().center.x;
        if(center < FIRST) return 1;
        if(center < SECOND) return 2;
        return 3;
    }
    @Override
    public void getData(Telemetry telemetry) {
    }
}