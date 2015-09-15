package com.google.appengine.demos.ImageCollage;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class CheckLogInStatus{
    public static String getStatus() {
        /*
        This script can:
            1) check if the current user is an approvedUser
        */
        //initialize the services that we need
        UserService userService = UserServiceFactory.getUserService();

        //our approved users
        String[] approvedUsers = {"nichkwon@gmail.com", "jhwang261@gmail.com", "tytytylerk@gmail.com"};

        //1 if the current user is approved; 0 otherwise
        int isApproved = 0;
        //get the current user
        User user = userService.getCurrentUser();
        if (user != null) {
            for (String approvedUser : approvedUsers) {
                //check if the email of the currentUser matches that of one of the approvedUsers
                if (approvedUser.compareTo(user.getEmail()) == 0) {
                    isApproved = 1;
                }
            }
        }
        String logIn;
        if (isApproved == 1){
            logIn = userService.createLogoutURL("/");
        }
        else{
            logIn = userService.createLoginURL("/");
        }
        return isApproved + " " + logIn;
    }
}
