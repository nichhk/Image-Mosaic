package com.google.appengine.demos.ImageCollage;

import com.google.appengine.tools.cloudstorage.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;

/*
UploadServlet: this class's doPost method is called when a user submits the form
to make a collage. It writes the url for the collage and the attribution mapping for
each thumbnail into the collage to the response.
 */

public class GetMosaicServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            //AppIdentityService aiService = AppIdentityServiceFactory.getAppIdentityService();
            //String bucketName = aiService.getDefaultGcsBucketName();
            String time = req.getParameter("time");
            GcsService gcsService =
                    GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
            GcsFilename filename = new GcsFilename("image-mosaic.appspot.com", time);
            int fileSize = (int) gcsService.getMetadata(filename).getLength();
            ByteBuffer result = ByteBuffer.allocate(fileSize);
            GcsInputChannel readChannel = gcsService.openReadChannel(filename, 0);
            readChannel.read(result);

            resp.setCharacterEncoding("UTF-8");

            resp.setContentType("application/json");
            resp.getWriter().write(new String(result.array()));
        } catch (Exception e) {
            resp.getWriter().write("not done");
        }
    }
}