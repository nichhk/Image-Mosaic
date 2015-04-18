package com.google.appengine.demos.ImageCollage;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HomeServlet extends HttpServlet{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] status = CheckLogInStatus.getStatus().split(" ");
        try {
            req.setAttribute("isApproved", status[0]);
            req.setAttribute("log", status[1]);
            req.setAttribute("page", "null");
            req.getRequestDispatcher("Home.jsp").forward(req, resp);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

