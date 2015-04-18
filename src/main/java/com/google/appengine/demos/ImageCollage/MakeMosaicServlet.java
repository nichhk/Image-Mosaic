package com.google.appengine.demos.ImageCollage;

import com.google.appengine.api.appidentity.*;
import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.files.*;
import com.google.appengine.api.images.*;
import com.google.appengine.tools.cloudstorage.*;
import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
/*
UploadServlet: this class's doPost method is called when a user submits the form
to make a collage. It writes the url for the collage and the attribution mapping for
each thumbnail into the collage to the response.
 */

public class MakeMosaicServlet extends HttpServlet {
    //create a BlobstoreService so that we can 1) get the user's photo; 2) serve the collage
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] status = CheckLogInStatus.getStatus().split(" ");
        try {
            req.setAttribute("isApproved", status[0]);
            req.setAttribute("log", status[1]);
            req.setAttribute("action", blobstoreService.createUploadUrl("/upload"));
            req.setAttribute("page", "make_mosaic");
            req.getRequestDispatcher("MakeMosaic.jsp").forward(req, resp);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int depth = Integer.parseInt(req.getParameter("depth"));
        int threshold = 0;
        boolean smartSizing = false;
        String smartSizingString = req.getParameter("smartSizing");
        if (smartSizingString.compareTo("true") == 0) {
            smartSizing = true;
            threshold = Integer.parseInt(req.getParameter("threshold"));
        }
        BlobKey blobKey = new BlobKey(req.getParameter("blobKeyName"));
        String time = req.getParameter("time");
        //begin making the collage
        CollageMaster master = new CollageMaster();
        ImagesService imgService = ImagesServiceFactory.getImagesService();
        Image collage = master.getCollage(imgService, blobKey, depth, threshold, smartSizing);
        //get the url for collage by adding it to the blobstore
        BlobKey mosaicBlobKey = toBlobstore(collage);
        String url = imgService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(mosaicBlobKey))+"=s1600";
        //now get the url and attribute wrapped together in an object in JSON format
        String urlAndAttribute = new Gson().toJson(new URLAndAttribute(url, master.getAttributionTable(), master.getX(), master.getY()));
        GcsService gcsService =
                GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
        GcsOutputChannel outputChannel =
                gcsService.createOrReplace(new GcsFilename("image-mosaic.appspot.com", time), GcsFileOptions.getDefaultInstance());
        outputChannel.write(ByteBuffer.wrap(urlAndAttribute.getBytes("UTF-8")));
        outputChannel.close();
        GcsOutputChannel outputChannel1 =
                gcsService.createOrReplace(new GcsFilename("image-mosaic.appspot.com", time+"_mosaic.png"), new GcsFileOptions.Builder().acl("public-read").build());
        outputChannel1.write(ByteBuffer.wrap(collage.getImageData()));
        outputChannel1.close();
    }

    /*
    inputs: Image uploadMe, the image that is to be uploaded to the blobstore
    returns the BlobKey for uploadMe
     */
    public static BlobKey toBlobstore(Image uploadMe){
        try {
            // Get a file service
            FileService fileService = FileServiceFactory.getFileService();

            // Create a new Blob file with mime-type "image/png"
            AppEngineFile file = fileService.createNewBlobFile("image/jpeg");// png

            // Open a channel to write to it
            boolean lock = true;
            FileWriteChannel writeChannel = fileService.openWriteChannel(file, lock);

            // This time we write to the channel directly
            writeChannel.write(ByteBuffer.wrap
                    (uploadMe.getImageData()));

            // Now finalize
            writeChannel.closeFinally();
            //return the blobKey for the file
            return fileService.getBlobKey(file);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /*
    URLAndAttribute wraps up the pertinent data for a completed collage:
        -String url, the url of the collage image
        -List<Collage.AttributionCell> attributionTable, which maps a pixel area to the url
            of the thumbnail that is in the pixel area
        -int width, the width of the collage
        -int height, the height of the collage
     */
    class URLAndAttribute{
        String url;
        List<Collage.AttributionCell> attributionTable;
        int width;
        int height;
        public URLAndAttribute(String url, List<Collage.AttributionCell> attributes, int width, int height){
            this.url = url;
            this.attributionTable = attributes;
            this.width = width;
            this.height = height;
            System.out.println("width is"+width);
            System.out.println("height is"+height);
        }
    }
}