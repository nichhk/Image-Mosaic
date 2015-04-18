package com.google.appengine.demos.ImageCollage;

import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.Transform;
import com.google.apphosting.api.ApiProxy;

import java.util.ArrayList;

/*
Update: 12/30
Built the class to catch time out errors from ImageServices and retry the transform
 */


public class Transformer {

    private ImagesService imgService;

    public Transformer(ImagesService imgService){
        this.imgService = imgService;
    }

    /*
    Attempts a crop or scale 4 times. If every time fails, then return the original image
    */

    public Image transform(Image input, Transform transform,int depth){

        if(depth>4){
            return input;
        }
        try {
            return imgService.applyTransform(transform, input);
        }catch(ApiProxy.ApiDeadlineExceededException e){
            e.printStackTrace();
            return transform(input,transform,depth++);
        }

    }

    /*
    Attempts to composite an image. If it fails, retries 4 more times. If all attempts fail, return a blank image
     */

    public Image composite(ArrayList<Composite> composites , int width, int height, long opac, int depth){

        if(depth>4){ // If the composite fails too many times, return a black image
            return imgService.composite(new ArrayList<Composite>(),width,height,opac);
        }
        try{
            return imgService.composite(composites,width,height,opac);
        }catch (ApiProxy.ApiDeadlineExceededException e){
            e.printStackTrace();
            return composite(composites, width, height, opac, depth++);
        }
    }
}