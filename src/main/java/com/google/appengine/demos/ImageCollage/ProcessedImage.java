package com.google.appengine.demos.ImageCollage;

 /*
 Update: 12/24
 Added in the scaled image cache
 ProcessedImage: Stores an image and its following data:
    - Author userName *
    - Author id *
    - Thumbnail URL *
    - Dimensions
    - RGB variance
    - RGB histogram
 * not stored for ProcessedImages that are constructed from Collage
 Also provides the following transformations:
    - Crop image
    - Rescale image
 */

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.photos.*;
import com.google.appengine.api.images.*;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class ProcessedImage{

    private ImagesService imagesService = ImagesServiceFactory.getImagesService();
    private Image img;
    private int width; // Width of the base image
    private int height; // Height of the base image
    private String url; // Image's Thumbnail Url
    private String userName; // Artist's Flickr user name
    private String userId; // Artist's Flickr id
    private HashMap<String, Image> cache = new HashMap<String, Image>();
    private String title; //title of the photo
    private Transformer transformer; // Wraps the Google Image API
    private String license;
    /*
    Constructs a ProcessedImage from an image Url.
    Called primarily from Collage.findImage(), downloads the image from its small url
    */

    public ProcessedImage(String url, String userName, String userId, String title) throws IOException{
        this.url = url;
        readImage(url); // Downloads the image from the Url
        getDim(); // Reads in the dimensions of the image
        this.userName = userName;
        this.userId = userId;
        this.title = title;
    }

    /*
    Constructs a ProcessedImage from a Flickr.photo object
    Called primarily from Crawler
    Inputs: photo: Flickr photo
    f: Flickr object
     */

    public ProcessedImage(Photo photo, Flickr f) throws IOException{
        url = photo.getThumbnailUrl(); // Stores the photo's thumbnail url
        readImage(url); // Downloads the image from the Url
        getDim(); // Reads in the dimensions
        readUsername(photo,f); // Finds the artist username from the Flickr Api
        this.userId = photo.getOwner().getId(); // Finds the artist id from the Flickr Api
        this.title = photo.getTitle();
    }

    /*
    Constructs a ProcessedImage from a photo and its meta-data
    Called primarily from Collage.findImage() and gets the image directly from the Database
    Input: photo: Google image object containing the photo
    url: Image's thumbnail url
    userName: Artist's username
    userId: Artist's id
     */

    public ProcessedImage(Image photo, String url, String userName, String userId, String title, String license){
        img = photo;
        getDim();
        this.url = url;
        this.userName = userName;
        this.userId = userId;
        this.title = title;
        this.license = license;
    }

    /*
    Reads an image in from a Url. Called from Collage.findImage() and Crawler.addToDatastore(). IOExceptions are handled
    by the calling functions
     */

    private Image readImage(String myURL) throws IOException {
        URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
        HTTPResponse fetchResponse = fetchService.fetch(new URL(myURL));
        img = ImagesServiceFactory.makeImage(fetchResponse.getContent());
        return img;

    }

    /*
    Finds the Username of a Flickr photo
     */

    private void readUsername(Photo photo, Flickr f){
        try {
            PeopleInterface member = f.getPeopleInterface();
            userName = member.getInfo(photo.getOwner().getId()).getUsername();
        }catch(Exception e){ // If the method fails to find the Username, assume it is unknown
            userName = "Unknown";
        }

    }

    /*
    Reads the height and width of the image
     */

    private void getDim(){
        width = img.getWidth();
        height = img.getHeight();
    }

    /*
    Creates and returns the RGB histogram of the image. The RGB histogram that we use has 8 evenly sized bins per color.
    Uses the 8 bins to prevent prohibitive query times within the LSH M-Tree
    */

    public double[] getRGBHistogram(){

        int[][] histogram = imagesService.histogram(img); // Uses the Google Image API to create a color histogram
        double[] rgbHistogram = new double[24]; // Our color histogram
        for (int color = 0; color < 3; color++){ // Iterate over red, green, and blue
            for (int magnitude = 0; magnitude < 8; magnitude++){ // Iterate over the 8 bins
                double sum = 0;
                for (int j = magnitude*32; j < magnitude*32 + 32; j++){ // Iterates over the 32 bins that correspond to our 1 bin
                    sum+= histogram[color][j]; // Finds the value of "our" bins
                }
                rgbHistogram[color*8+magnitude] = sum; // Sets the bin value
            }
        }
        return normalizeArray(rgbHistogram); // Normalizes the histogram for the distance queries in the LSH M-Tree

    }

    /*
    Finds and returns the totaled variance of the RGB values of the image
     */

    public double getVariance(){
        int[][] histogram = imagesService.histogram(img); // Creates a histogram using the Google Image API
        double[] average = new double[3]; // The 3 average values of red, green and blue
        double[] squareAverage = new double[3]; // The 3 squared average values of red, green and blue
        int dim = img.getHeight()*img.getWidth(); // Number of pixels in the image

        //get the sum for each row
        for (int color = 0; color < 3; color++){ // Iterate over each of the colors
            for (int bin = 0; bin < 256; bin++){ // Iterate over each of the bins
                average[color] += histogram[color][bin]*bin; // The total is equal to the number of pixels in a given bin x the bin's brightness
                squareAverage[color] += histogram[color][bin]*Math.pow(bin,2); // The squared total of the pixels
            }
        }

        double variance = 0; // Tracks the total variance
        for (int color = 0; color < 3; color++){ // Iterates over each of the colors
            average[color] /= dim; // Finds the average by dividing the totals by the number of pixels
            squareAverage[color] /= dim; // Finds the squared averages by dividing squared totals by the number of pixels
            variance += squareAverage[color] - Math.pow(average[color],2); // Sums the variance for each of the color channels
        }
        return variance;
    }

    /*
    Returns a copy of a subsection of the current image
    Inputs: firstX: X coordinate of the upper-left corner of the subsection
    firstY: Y coordinate of the upper-left corner of the subsection
    partitionHeight: Height of the subsection
    partitionWidth: Width of the subsection
     */

    public ProcessedImage getBlock(int firstX, int firstY, int partitionHeight, int partitionWidth){
        Transform cropBlock = ImagesServiceFactory.makeCrop((float)firstX/width, (float)firstY/height, (float)(firstX+partitionWidth)/width, (float)(firstY+partitionHeight)/height);
        // Defines the crop within the percentage values of the coordinates, according to the Google Image API
        Image cropped = ImagesServiceFactory.makeImage(img.getImageData());
        // Returns a new ProcessedImage that is a cropped copy of this ProcessedImage
        ProcessedImage returnVal = new ProcessedImage(transformer.transform(cropped,cropBlock,0),null,null, null, null, null);
        returnVal.setTransformer(transformer);
        return returnVal;
    }

    /*
    Returns a scaled copy of the image. First checks the cache for a scaled image of the same size. If the image
    is not in the cache, copy the image data, rescale, and add to the cache.
    Inputs:x: New width of the image
    y: new height of the image
    depth: Recursion level of the required image
    */

    public Image getScaled(int x, int y, int depth){
        String key = Integer.toString(x)+" "+Integer.toString(y); // A string corresponding to the required scale dimensions
        if(cache.containsKey(key)){ // Checks to see if the image has been scaled to these dimensions before
            return cache.get(key); // Returns this copy of the image
        }
        else{ // Else we must re-scale the images ourselves
            Image toScale = ImagesServiceFactory.makeImage(img.getImageData()); // Transform mutates the input image
            if(x*y>15000){ // If the required scaled image is much larger than a thumbnail...
                try {
                    toScale = readImage(getSmallUrl()); // Download a higher resolution version of the image
                }catch(Exception e){ // If there is an IOException, just use the thumbnail image
                    //e.printStackTrace();
                }
            }
            Transform scaleTransform = ImagesServiceFactory.makeResize(x, y, true); // Re-scale the image
            Image scaled = transformer.transform(toScale,scaleTransform,0);
            cache.put(key,scaled); // Place the scaled image into the cache
            return scaled; // Return the scaled image
        }
    }


    /*
    Normalizes a numeric array
    */

    private double[] normalizeArray(double[] rgbHist){
        double[] normalized = new double[rgbHist.length]; // Creates a new array of equal length
        double sum = 0; // Tracks the total sum of the old array

        for (double element : rgbHist){ // Sums each element of the old array
            sum += element;
        }

        for (int i = 0; i < rgbHist.length; i++){ // Divide each element in the old array by the total sum
            normalized[i] = rgbHist[i]/sum;
        }
        return normalized;

    }

    /*
    Returns the Image
     */

    public Image getImage(){
        return img;
    }

    /*
    Returns the Url for a Small copy of the image, which is higher resolution than the thumbnail image
     */

    public String getSmallUrl(){
        String returnVal = url.replace("t.jpg","m.jpg");
        // The Small Url is the same as the Thumbnail's, except for a single character change at the end of the Url
        return returnVal;
    }

    /*
    Returns the image's artist's Flickr id
     */

    public String getId(){
        return userId;
    }

    /*
    Returns the Url of the image thumbnail
     */

    public String getUrl(){
        return url;
    }

    /*
    Returns the image's artist's Flickr username
    */

    public String getUsername(){
        return userName;
    }

    public String getTitle(){
        return title;
    }
    public void setTransformer(Transformer transformer){
        this.transformer = transformer;
    }

    public String getLicense(){
        return license;
    }
}