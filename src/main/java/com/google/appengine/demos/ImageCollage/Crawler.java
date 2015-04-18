package com.google.appengine.demos.ImageCollage;

import be.hogent.tarsos.lsh.Index;
import be.hogent.tarsos.lsh.Vector;
import be.hogent.tarsos.lsh.families.EuclidianHashFamily;
import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;
import com.flickr4java.flickr.photos.licenses.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/*
Crawler: This class enables users to crawl flickr for images and add them to the datastore.
It can also crawl the datastore for thumbnails that are most similar to an inputted image.
 */
public class Crawler {
    //authentication for flickr API
    private String apiKey = "c3916472c30d567c38898c61ee7d0638";
    private String sharedSecret = "06cd65d9183f0d70";
    //flickr object
    private Flickr f;
    //object to search flickr
    private PhotosInterface finder;
    //index built on LSH
    private Index index;
    //the GAE datastore
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    /*
    constructor for the crawler
     */
    public Crawler() {
        //try to make the Flickr object
        try {
            f = new Flickr(apiKey, sharedSecret, new REST());
            finder = f.getPhotosInterface();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //arguments to EuclidianHashFamily are : int w (which seems arbitrary), int lengthOfVectors
        EuclidianHashFamily hashFam = new EuclidianHashFamily(10, 24);
        //arguments to Index are : a HashFamily, int numHashTables, int numHashes
        index = new Index(hashFam, 5, 5);
    }

    /*
    inputs: String topics, the search parameters
            int numImg, the number of images we want to get from flickr
    Downloads numImg photos related to topics from flickr
     */
    public void getPhotos(String topics, int numImg) {
        PhotoList<Photo> photos = new PhotoList<Photo>();
        SearchParameters param = new SearchParameters();
        String[] tags = new String[1];
        tags[0] = topics;
        param.setTags(tags);
        //we can only download images licensed with CC
        param.setLicense("1,2,4,5,7");
        try {
            photos = finder.search(param, numImg, 1);
            //get the time of the search so that we can store it
            String when = new Date().toString();
            DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            //parse the String when into a Date object
            //we need to do this to remove some of the precision of the long time
            Date date = df.parse(when);
            long time = date.getTime();
            //make an Entity for the search
            Entity search = new Entity("search", date.toString());
            search.setProperty("time", time);
            System.out.println("The search occured at " + time);
            search.setProperty("searchParam", topics);
            search.setProperty("numImg", numImg);
            //put the search into the datastore
            datastore.put(search);
            //put the photos we found into the datastore
            addToDatastore(photos, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    inputs: PhotoList<Photo> photos, the photos we found using getPhotos
            long time, the time at which the search was executed
    adds all of the photos in photos to the datastore
     */
    public void addToDatastore(PhotoList<Photo> photos, long time) {
        //iterate over all of the photos in the list
        for (Photo pic : photos) {
            try {
                //make a ProcessedImage for the photo
                ProcessedImage processed = new ProcessedImage(pic, f);
                //this makes it easy to get an rgb histogram for the photo
                double[] rgbHist = processed.getRGBHistogram();

                //the key is the photo's url, creator, and id
                String key = processed.getUrl() + "," + processed.getUsername() + "," + processed.getId();
                //set key as the key for the flickrPic

                Entity flickrPic = new Entity("flickrPic", key);
                //now we're gonna iterate over the rgbHist and set the values as properties to the flickrPic
                for (int i = 0; i < rgbHist.length; i++) {
                    double binVal = rgbHist[i];
                    //if i < 8, then we're iterating over the red bins
                    if (i < 8) {
                        flickrPic.setProperty("r" + Integer.toString(i), binVal);
                    }
                    //we're iterating over the green bins
                    else if (i < 16) {
                        flickrPic.setProperty("g" + Integer.toString(i - 8), binVal);
                    }
                    //we're iterating over the blue bins
                    else {
                        flickrPic.setProperty("b" + Integer.toString(i - 16), binVal);
                    }
                }
                //set the property blob to be the byte array for the image
                flickrPic.setProperty("blob", new Blob(processed.getImage().getImageData()));
                //set the time property
                flickrPic.setProperty("time", time);
                //set the title of the photo
                flickrPic.setProperty("title", pic.getTitle());
                Photo newPhoto = finder.getInfo(pic.getId(), sharedSecret);
                flickrPic.setProperty("license", newPhoto.getLicense());
                //add the flickrPic to the datastore
                datastore.put(flickrPic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Adds all of the flickrPics in our datastore to the index so that we can quickly
    executed nearest neighbor searches
     */
    public void buildIndex() {
        //make a query for the flickPics
        Query getAll = new Query("flickrPic");
        //get all of them
        PreparedQuery allPics = datastore.prepare(getAll);
        //i is the number of flickrPics in our datastore
        int i = 0;
        //iterate over all pics
        for (Entity result : allPics.asIterable()) {
            //get the rgbHist from the result
            double[] rgbHist = getArrayFromEntity(result);
            //get the key
            String key = result.getKey().getName();
            //make a Vector from the rgbHist and name it key
            Vector vec = new Vector(key, rgbHist);
            //add the vector to our index
            index.index(vec);
            i++;
        }
        System.out.println("Added " + i + " photos to the LSH.");
    }

    /*
    input: Entity result, the entity for which we want to get an rgbHistogram
     */
    public double[] getArrayFromEntity(Entity result) {
        //initialize our array
        double[] array = new double[24];
        //iterate over each color property in the result
        for (int i = 0; i < 24; i++) {
            //this is the name of the property we want to get from result
            String propertyName;
            //if i is less than 8, then we want to get the ith red property
            if (i < 8) {
                propertyName = "r" + Integer.toString(i);
            }
            //if i is less than 16, we want to get the (i-8)th green property
            else if (i < 16) {
                propertyName = "g" + Integer.toString(i - 8);
            }
            //we want to get the (i-16)th blue property
            else {
                propertyName = "b" + Integer.toString(i - 16);
            }
            //set the array[i] to result's value for propertyName
            array[i] = (double) result.getProperty(propertyName);
        }
        return array;
    }

    /*
    inputs: double[] rgbHistogram, a 24 dim. point for which we want to find the nearest neighbor in index
            boolean dither, true if we want to add some randomness to choosing the nearest neighbor; false otherwise
    returns the ProcessedImage for (dither ? one of the 3 closest : the closest) vector to rgbHistogram in index
     */
    public ProcessedImage query(double[] rgbHistogram, boolean doDither) {
        //make a vector based on the rgbHistogram
        Vector vector = new Vector("", rgbHistogram);
        //if dither, get the 3 closest vectors to vector; get the closest vector to vector otherwise
        List<Vector> closest = index.query(vector, doDither ? 3 : 1);
        //whichOne is the index of the vector we want to get from closest
        int whichOne = 0;
        //if we're dithering, we want to randomly choose one of the three closest vectors in closest
        if (doDither) {
            Random random = new Random();
            //generate a random double
            double dither = random.nextDouble();
            //50% chance of choosing the closest
            if (dither < .5) {
                whichOne = 0;
            }
            //35% chance of choosing the 2nd closest
            else if (dither < .85) {
                whichOne = 1;
            }
            //15% chance of choosing the 3rd closest
            else {
                whichOne = 2;
            }
        }
        try {
            //get the key for the vector
            String key = closest.get(whichOne).getKey();
            //get the entity for that key
            Entity closestEnt = datastore.get(KeyFactory.createKey("flickrPic", key));
            //make an image from the blob in closestEnt
            Image returnVal = ImagesServiceFactory.makeImage(((Blob) closestEnt.getProperty("blob")).getBytes());
            //return the ProcessedImage for returnVal
            String[] metaInfo = key.split("\\s");
            return new ProcessedImage(returnVal, metaInfo[0], metaInfo[1], metaInfo[2],
                    (String)closestEnt.getProperty("title"), (String)closestEnt.getProperty("license"));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String ditherQueryKey(double[] rgbHistogram) {
        Vector vector = new Vector("", rgbHistogram);
        List<Vector> closest = index.query(vector, 3);
        Random random = new Random();
        double dither = random.nextDouble();
        int whichOne = 0;
        if (dither < .5) {
            whichOne = 0;
        } else if (dither < .85) {
            whichOne = 1;
        } else {
            whichOne = 2;
        }
        return closest.get(whichOne).getKey();
    }

    public ProcessedImage loadImage(String key) {
        try {
            Entity closestEnt = datastore.get(KeyFactory.createKey("flickrPic", key));
            Image returnVal = ImagesServiceFactory.makeImage(((Blob) closestEnt.getProperty("blob")).getBytes());
            return new ProcessedImage(returnVal, key.split(",")[0], key.split(",")[1], key.split(",")[2],
                    (String)closestEnt.getProperty("title"), (String)closestEnt.getProperty("license"));
        } catch (EntityNotFoundException e) {
            return null;
        }
    }
}
