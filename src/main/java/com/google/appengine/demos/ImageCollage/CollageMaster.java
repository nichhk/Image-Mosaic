package com.google.appengine.demos.ImageCollage;

/*
 Update: 12/24
 Added general ratios: 4x4, 3x5, 2x6
 CollageMaster: Controls the many Collage threads that are called on an image. Produces the composited collage and
 also produces the composited AttributionTable for the collage.
*/

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.images.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadFactory;


public class CollageMaster {

    private List<Collage.AttributionCell> attributionTable = new ArrayList<Collage.AttributionCell>();

    // A composited list of the AttributionCell's of the sub-collages

    private int width; // Width of the image to be collaged
    private int height; // Height of the image to be collaged
    private CollageTimer collageTimer; // Used to halt execution of threads at a specified time limit
    private HashMap<String, ProcessedImage> imageCache = new HashMap<String,ProcessedImage>();

    // Caches scaled images to reduce the amount of time using Image.scale()

    private Image image;
    private byte[] imageData;
    private int trueWidth, trueHeight;
    /*
    Controls the execution of multiple threads that will create the overall collage
    Inputs: imgService: Google API ImagesService
    blobKey: Blob key corresponding to the image to be collaged
    depth: Maximum recursion depth of the collage
    threshold: Variance threshhold of the collage
     */

    public Image getCollage(ImagesService imgService, BlobKey blobKey, int depth, int threshold, boolean smartSize){

        try {
            collageTimer = new CollageTimer(52.5); // Used to trigger the Quick-Finish in the Collage objects

            // Must be determined empirically, along with the weights in the Collage.colorBlock() method

            Crawler crawler = new Crawler(); // Provides access to the image database
            crawler.buildIndex(); // Builds the LSH M-Tree for the images
            Transformer transformer = new Transformer(imgService);
            imageData = getData(blobKey); // Finds the base image submitted by the user
            image = ImagesServiceFactory.makeImage(imageData);
            width = image.getWidth(); // Dimensions of the base-image
            height = image.getHeight();
            int[] initSplit = findRatio() ; // Splits the base-image into initSplit[0] x initSplit[1] smaller images
            rescaleImage(initSplit,transformer);
            ThreadFactory tf = ThreadManager.currentRequestThreadFactory();
            ArrayList<Collage> subCollages = new ArrayList<Collage>(); // ArrayList of the subCollage threads
            ArrayList<Thread> threads = new ArrayList<>();
            ArrayList<Thread> rows = new ArrayList<Thread>();

            for (int i = 0; i < initSplit[0]; i++) { // Create 4 rows
                Thread t = tf.newThread(new generateRow(i,initSplit,depth,threshold,crawler,subCollages,threads,tf,smartSize)); //Create a thread object using the task object created
                t.start(); // Begin the thread as soon as possible
                rows.add(t);
            }
            for(Thread thread: rows){
                thread.join();
            }
            int z = 0;
            for (Thread thread : threads){ // Compile the completed threads
                thread.join();
                z++;
            }
            ArrayList<Composite> composites = new ArrayList<>();
            ImagesService.OutputEncoding output = ImagesService.OutputEncoding.PNG; // We want to return the image as a JPEG
            for(Collage collage: subCollages){
                composites.add(ImagesServiceFactory.makeComposite(collage.getCollage(),
                        collage.getInitX(),collage.getInitY(), 1f, Composite.Anchor.TOP_LEFT));

                // Composite the 16 sub-collages

                attributionTable.addAll(collage.getAttributionTable());

                // Compile the attribution cells from each of the collages

            }
            return imgService.composite(composites, width, height, 0,output);
        }
        catch (Exception e) {
            System.out.println("Okay, here is the error that we're getting from master.makeCollage()");
            e.printStackTrace();
            return null;
        }

    }

    /*
    Reads the image data from a blobkey
    */

    private byte[] getData(BlobKey blobKey) {

        InputStream input;
        byte[] oldImageData = null;
        try {
            input = new BlobstoreInputStream(blobKey);
            ByteArrayOutputStream bais = new ByteArrayOutputStream();
            byte[] byteChunk = new byte[4096];
            int n;
            while ((n = input.read(byteChunk)) > 0) {
                bais.write(byteChunk, 0, n);
            }
            oldImageData = bais.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return oldImageData;

    }

    public List<Collage.AttributionCell> getAttributionTable(){
        return attributionTable;
    }
    public int getX(){
        return width;
    }
    public int getY(){
        return height;
    }

    /*
    Fits the base image to one of the predetermined photo ratios. The returned int array contains
    the number of width, then height partitions.
     */

    public int[] findRatio(){

        double ratio = ((double)width)/height;
        if(ratio<0.14){
            return new int[]{2,7};
        }
        if(ratio < 0.333){
            return new int[]{3,5};
        }
        else if(ratio < 3){
            return new int[]{4,4};
        }
        else if(ratio<7){
            return new int[]{5,3};
        }
        else{
            return new int[]{7,2};
        }

    }

    private void rescaleImage(int[] newScale, Transformer transformer){

        int limit = 2500000; // Automatically rescales base-images that are larger than this pixel threshold
        double scalingFactor = 1; // This variable is obsolete
        if(!((width/(double)height)>6.4 || (height/(double)width)>6.4)) {
            scalingFactor = Math.pow((double) limit / (height * width), .5);
        }
        trueWidth = (int)Math.round(Math.ceil(scalingFactor*width/newScale[0]));
        int xScalingFactor = trueWidth*newScale[0];
        trueHeight = (int)Math.round(Math.ceil(scalingFactor*height/newScale[1]));
        int yScalingFactor = trueHeight*newScale[1];

        // Rounds the width and height to match the dimensions, so the partitions are of equal sizes

        Transform scaleTransform = ImagesServiceFactory.makeResize(xScalingFactor, yScalingFactor); // Rescales the image
        image = transformer.transform(image,scaleTransform,0);
        imageData = image.getImageData(); // Copies the new image data
        width = image.getWidth(); // Dimensions of the base-image
        height = image.getHeight();
    }

    private class generateRow implements Runnable{
        private int i;
        private int[] initSplit;
        private int depth;
        private Crawler crawler;
        private ArrayList<Collage> subCollages = new ArrayList<Collage>(); // ArrayList of the subCollage threads
        private ArrayList<Thread> threads = new ArrayList<Thread>();
        private Transformer transformer = new Transformer(ImagesServiceFactory.getImagesService());
        private int threshold;
        private ThreadFactory tf;
        private boolean smartSize;
        public generateRow(int i, int[] initSplit, int depth, int threshold, Crawler crawler, ArrayList<Collage> subCollages,
                           ArrayList<Thread> threads, ThreadFactory tf, boolean smartSize){
            this.i = i;
            this.initSplit = initSplit;
            this.depth = depth;
            this.crawler = crawler;
            this.subCollages = subCollages;
            this.threads = threads;
            this.threshold = threshold;
            this.tf = tf;
            this.smartSize = smartSize;
        }
        public void run(){
            double x = (double)i / initSplit[0];
            double xProp = 1.0 / initSplit[0];
            double yProp = 1.0 / initSplit[1];
            Image row = ImagesServiceFactory.makeImage(imageData); // Copies the base image
            Transform crop = ImagesServiceFactory.makeCrop(x, 0, x + xProp, 1);
            row = transformer.transform(row,crop,0); // Creates a copy of the specified row

            for (int j = 0; j < initSplit[1]; j++) { // Loops over the columns
                Image newImage = ImagesServiceFactory.makeImage(row.getImageData()); // Copies the base-row each time
                double y = (double)j / initSplit[1];
                Transform crop2 = ImagesServiceFactory.makeCrop(0, y, 1, y + yProp); // Crop the sub-image from the base-image
                newImage = transformer.transform(newImage,crop2,0);
                if (newImage.getHeight() != trueHeight || newImage.getWidth() != trueWidth){
                    Transform resizeToTrue = ImagesServiceFactory.makeResize(trueWidth, trueHeight, true);
                    newImage = transformer.transform(newImage, resizeToTrue, 0);
                }
                Collage subCollage = new Collage(newImage, depth - 3, threshold, crawler, (int)(x * width),
                        (int)(y * height), collageTimer, imageCache, tf, smartSize);
                subCollages.add(subCollage); //create the object of FutureTask
                Thread t = tf.newThread(subCollage); //Create a thread object using the task object created
                t.start(); // Begin the thread as soon as possible
                threads.add(t); // Adds the thread to the list of threads

            }
        }
    }
}