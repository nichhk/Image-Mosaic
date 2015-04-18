package com.google.appengine.demos.ImageCollage;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;
import com.flickr4java.flickr.photos.licenses.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;
import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
CrawlerServlet: a servlet that allows approved users to
    1) crawl flickr for photos and add them to the datastore
    2) check the photos in the datastore and delete those which are undesireable
    3) see all of the previous searches to flickr
 */

public class CrawlerServlet extends HttpServlet {

    //almost every possible use of the CrawlerServlet needs to access the datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    /*
    doGet defines what to do when a GET request is sent to the server.
    With a GET request, the user either wants to see all of the previous
    CrawlerSearches or the images downloaded from a particular CrawlerSearch.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String get = req.getParameter("get");
        //if get is null, then that means that the user is trying to search flickr and add photos to the datastore
        if (get == null){
            //get what the user wants to search for
            String searchParam = req.getParameter("searchParam");
            //and how many photos to get
            int howMany = Integer.parseInt(req.getParameter("howMany"));
            //and then crawl
            crawl(searchParam, howMany);
        }
        //otherwise, we want to get either the CrawlerSearches or the images from a specific CrawlerSearch
        else {
            String writeMe = "";
            //if the get parameter is crawlerSearches, then set writeMe to the JSON string for crawlerSearches
            if (get.compareTo("crawlerSearches") == 0){
                writeMe = getCrawlerSearches();
            }
            //otherwise, the get parameter is a time.
            //set writeMe to the JSON string defining images searched at time=get
            else {
                writeMe = getImages(get);
            }
            //write writeMe to the response
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(writeMe);
        }
    }

    /*
    The user needs to POST when trying to delete images, since the request parameters (which photos to delete)
    might be too large to GET.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        String[] deleteThem = req.getParameterValues("deleteMe");
        deleteImages(deleteThem);
    }

    /*
    inputs: String searchParam, what the user wants to search for
            int howMany, how many photos to get
     */
    private void crawl(String searchParam, int howMany){
        //instantiate a Crawler object
        Crawler crawl = new Crawler();
        //get the photos
        crawl.getPhotos(searchParam, howMany);
    }

    /*
    Returns all of the CrawlerSearches that have been made as a JSON string
     */
    private String getCrawlerSearches(){
        //query all of the search entities in the datastore
        Query q = new Query("search");
        PreparedQuery pq = datastore.prepare(q);
        List<CrawlerSearch> searches = new ArrayList<CrawlerSearch>();
        //iterate over the results
        for (Entity result : pq.asIterable()){
            //make a CrawlerSearch object for the result and add it to searches
            searches.add(new CrawlerSearch(new Date((long)result.getProperty("time")).toString(), (String)result.getProperty("searchParam"), (int)(long)result.getProperty("numImg")));
        }
        //turn searches into a JSON string and return it
        return new Gson().toJson(searches);

    }

    /*
    inputs: String time, the user wants to get all the photos that were added to the datastore at time

    Returns a JSON string consisting of the urls of the photos downloaded at time
     */
    private String getImages(String time){
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        try {
            //parse the time into a Date object
            Date date = df.parse(time);
            //System.out.println("we want to get images that were searched for at time "+ date.getTime());
            //make a filter to only search for entities that occurred at time
            Filter timeEquals = new FilterPredicate("time", FilterOperator.EQUAL,
                    date.getTime());
            //make a query for flickrPics with timeEquals
            Query q = new Query("flickrPic").setFilter(timeEquals);

            // Use PreparedQuery interface to retrieve results
            PreparedQuery pq = datastore.prepare(q);
            List<String> urls = new ArrayList<String>();
            //iterate over the results and add their names to urls
            for (Entity result : pq.asIterable()){
                urls.add(result.getKey().getName());
            }
            //turn urls into a JSON string and return it
            return new Gson().toJson(urls);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /*
    inputs: String[] deleteUs, the keys of the images that user wants to delete

    Deletes the photos defined by deleteUs
     */
    private void deleteImages(String[] deleteUs){
        //for each photo that we want to delete
        for (String deleteMe : deleteUs){
            //make a key based on the string
            Key key = KeyFactory.createKey("flickrPic", deleteMe);
            //delete the entity corresponding to key
            datastore.delete(key);
        }
    }

    /*
    CrawlerSearch: a class that wraps all the pertinent parameters when a search is executed:
        1) String time, the time at which the search was executed
        2) String searchParam, what the user searched for
        3) int numImg, the number of images the user wanted to download
     */
    class CrawlerSearch{
        String time;
        String searchParam;
        int numImg;
        public CrawlerSearch(String time, String searchParam, int numImg){
            this.time = time;
            this.searchParam = searchParam;
            this.numImg = numImg;
        }
    }
}