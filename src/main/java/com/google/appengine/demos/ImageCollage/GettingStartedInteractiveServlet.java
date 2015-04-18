package com.google.appengine.demos.ImageCollage;

import com.google.appengine.tools.cloudstorage.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by compsci on 1/5/15.
 */
public class GettingStartedInteractiveServlet  extends HttpServlet {
    private String JSONfile;
    private int size, height, width;
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] status = CheckLogInStatus.getStatus().split(" ");
        try {
            req.setAttribute("isApproved", status[0]);
            req.setAttribute("log", status[1]);
            req.getAttribute("pictureID");
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            req.setAttribute("page", "getting_started");
            String filePath = getFilePath(Integer.parseInt(req.getParameter("picNum")));
            GcsService gcsService =
                    GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
            GcsFilename filename = new GcsFilename("image-mosaic.appspot.com", JSONfile);
            int fileSize = (int) gcsService.getMetadata(filename).getLength();
            ByteBuffer result = ByteBuffer.allocate(fileSize);
            GcsInputChannel readChannel = gcsService.openReadChannel(filename, 0);
            readChannel.read(result);
            req.setAttribute("filePath",filePath);
            req.setAttribute("width",width);
            req.setAttribute("height",height);
            req.setAttribute("attributionMap", new String(result.array()).replace("\n", "").replace("\r", ""));
            req.setAttribute("size", size);
            req.getRequestDispatcher("GettingStartedInteractive.jsp").forward(req, resp);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public String getFilePath(int numPic) {
        height = 817;
        width = 1228;
        if (numPic == 1) {
            JSONfile = "SmartBirdHTML.txt";
            size = 2926;
            return "Pictures/Images/BirdSmart.png";
        } else if (numPic == 2) {
            JSONfile = "DumbBirdHTML.txt";
            size = 4096;
            return "Pictures/Images/BirdDumb.png";
        } else if (numPic == 3) {
            JSONfile = "Drum6HTML.txt";
            size = 1024;
            return "Pictures/Images/Drum6.png";
        } else if (numPic == 4) {
            JSONfile = "Drum8HTML.txt";
            size = 16384;
            return "Pictures/Images/Drum8.png";
        } else if (numPic == 5) {
            JSONfile = "Parrot500HTML.txt";
            size = 3523;
            return "Pictures/Images/Parrot500.png";
        } else if (numPic == 6) {
            JSONfile = "Parrot1500HTML.txt";
            size = 2689;
            return "Pictures/Images/Parrot1500.png";
        } else {
            JSONfile = "StarNight.txt";
            size = 16384;
            height = 971;
            width = 1552;
            return "images/starryNight.png";
        }
    }
}