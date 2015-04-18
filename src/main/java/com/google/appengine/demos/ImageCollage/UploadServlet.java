package com.google.appengine.demos.ImageCollage;

import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.files.*;
import com.google.appengine.api.images.*;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;
import static com.google.appengine.api.taskqueue.RetryOptions.Builder.*;

/*
UploadServlet: this class's doPost method is called when a user submits the form
to make a collage. It writes the url for the collage and the attribution mapping for
each thumbnail into the collage to the response.
 */

public class UploadServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        //get the blob for the submitted image
        Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
        BlobKey blobKey = blobs.get("userPic");
        //get the rest of the parameters
        int depth = Integer.parseInt(req.getParameter("depth"));
        int threshold = 0;
        boolean smartSizing = false;
        String smartSizingString = req.getParameter("smartSizing");
        if (smartSizingString.compareTo("true") == 0) {
            smartSizing = true;
            threshold = Integer.parseInt(req.getParameter("threshold"));
        }
        String time = req.getParameter("time");
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(withUrl("/make_mosaic").param("depth", String.valueOf(depth)).param("smartSizing", String.valueOf(smartSizing))
                .param("threshold", String.valueOf(threshold)).param("blobKeyName", blobKey.getKeyString()).param("time", time)
                .retryOptions(withTaskRetryLimit(1)));
    }
}