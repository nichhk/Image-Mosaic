package com.google.appengine.demos.ImageCollage;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

public class ContactServlet extends HttpServlet{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] status = CheckLogInStatus.getStatus().split(" ");
        try {
            req.setAttribute("isApproved", status[0]);
            req.setAttribute("log", status[1]);
            req.setAttribute("page", "contact");
            req.getRequestDispatcher("Contact.jsp").forward(req, resp);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String name = (String)req.getParameter("inputName");
        System.out.println(name);
        String email = (String)req.getParameter("inputEmail");
        System.out.println(email);
        String msgBody = (String)req.getParameter("inputMessage");
        System.out.println(msgBody);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("image.mosaic.app@gmail.com", "Image Mosaic Bot"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("nichkwon@gmail.com", "Nicholas Kwon"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("image.mosaic.app@gmail.com", "Admins"));
            msg.setSubject(name + " contacted you from image-mosaic.appspot.com");
            System.out.println("Reply to: " + email + "\n" +msgBody);
            msg.setText("Reply to: " + email + "\n" +msgBody);
            Transport.send(msg);

        } catch (AddressException e) {
            resp.getWriter().write("bad email");
        }
        catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}