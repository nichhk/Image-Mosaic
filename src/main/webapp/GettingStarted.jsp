<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<style>
    body,
    .center{
        text-align:center;
        background: none !important;
    }
    .site-wrapper{
         background-color: rgba(50,50,50,0.4);
    }
    .container-fluid a{
        color: white;
        text-decoration: underline;
    }
    .container-fluid a:hover{
        color: red;
    }
    footer{
        color:white;
    }
</style>
<t:template isApproved="${isApproved}" log="${log}" page="${page}">

    <jsp:attribute name="scripts">
     <script>
         $(function () {
             $(".linkImage").click(function(){
                 //alert("hi");
                 //alert(this.name);
                 document.getElementById('picNum').value=this.name;
                 //alert(document.getElementById('picNum').value);
                 $("#form-id").submit();
             });
         });
     </script>
    </jsp:attribute>

    <jsp:attribute name="content">

        <div class = "container-fluid" style="color:white">
            <div class="row">
                <div class = center class="inner cover" style="text-align: left">
                    <h1 class = center> Getting Started </h1>
                    <h3 style="text-align: left; padding-left: 30">Overview</h3>
                    <p style="padding:5 50">
                        This web app maintains a diverse selection of Flickr photos. Each photo is licensed with
                        a <a href="https://creativecommons.org/licenses/" target="_blank">Creative Commons license</a> that allows
                        the use and modification of the photo under certain conditions.
                        When you input an image, our photomosaic algorithms break it into several blocks. Then, for each block, it
                        queries our database using <a href="https://github.com/JorenSix/TarsosLSH" target="_blank">locality-sensitive hashing</a>
                        to quickly find the Flickr photo with the most similar colors.
                        The rest of this page will explain the different options for your mosaic.
                    </p>
                </div>
                <div class="row">
                    <div class = center class="inner cover" style="text-align: left">
                        <h3 style="text-align: left; padding-left: 30">Smart-sizing</h3>
                        <p style="padding:5 50">
                            We offer two styles for a mosaic: dynamically-sized blocks and statically-sized blocks.
                            The former, referred to as "smart-sizing," is preferable when the image has a
                            focal point, while statically-sized blocks are better for landscapes.
                            Smart-sizing represents uniformly-colored areas with a few large blocks, while
                            color-variant areas have a larger number of smaller blocks. This smart-sizing algorithm is more
                            efficient and allows for greater detail where it matters.
                        </p>
                    </div>

                </div>

                <div class="row">
                    <div class="col-md-4 col-md-push-4">
                        <h4>Original Image</h4>
                        <a href="https://www.flickr.com/photos/deniwlp84/14451399403/" target="_blank">
                            <img src= "Pictures/Images/Bird.jpg" class = "img-responsive img-thumbnail">
                        </a>

                    </div>

                    <div class="col-md-4 col-md-pull-4 col-sm-4">
                        <h4>Mosaic with Smart-sizing</h4>

                        <a class = "zoomImage" href="#">
                            <img name = "1" id = "imageresource" src = "Pictures/Images/BirdSmart.jpg" class= "img-responsive img-thumbnail linkImage" >
                        </a>

                    </div>
                    <div class="col-md-4 col-sm-4">
                        <h4>Mosaic without Smart-sizing</h4>
                        <a class = "zoomImage" href="#">
                            <img name = "2" src= "Pictures/Images/BirdDumb.jpg" class = "img-responsive img-thumbnail linkImage">
                        </a>

                    </div>


                </div>
                <p class="center"><a href="https://www.flickr.com/photos/deniwlp84/14427944351/" target="_blank">Bird's Park by Deni Williams</a> </p>
                <div class="row">
                    <div class = center class="inner cover" style="text-align: left">
                        <h3 style="text-align: left; padding-left: 30"> Depth</h3>
                        <p style="padding:5 50">
                            Depth determines the minimum size of a block. Therefore, higher depth values correspond
                            to smaller minimum sizes and therefore higher quality mosaics. Currently, the maximum depths
                            for non-smart-size mosaics and smart-size mosaics are 8 and 9, respectively. You can
                            <a href="/contact">contact</a> us to request a mosaic with a higher depth.
                        </p>
                    </div>

                </div>

                <div class="row">
                    <div class="col-md-4 col-md-push-4">
                        <h4>Original Image </h4>
                        <a href="https://www.flickr.com/photos/deniwlp84/14427944351/" target="_blank">
                            <img src="Pictures/Images/Drums.jpg" class = "img-responsive img-thumbnail">
                        </a>

                    </div>
                    <div class="col-md-4 col-md-pull-4 col-sm-4">
                        <h4>Depth 6</h4>
                        <a class = "zoomImage" href="#">
                            <img name = "3" src= "Pictures/Images/Drum6.jpg" class = "img-responsive img-thumbnail linkImage">
                        </a>
                    </div>
                    <div class="col-md-4 col-sm-4">
                        <h4>Depth 8 </h4>
                        <a class = "zoomImage" href="#">
                            <img name = "4" src= "Pictures/Images/Drum8.jpg" class = "img-responsive img-thumbnail linkImage">
                        </a>
                    </div>


                </div>
                <p class="center"><a href="https://www.flickr.com/photos/deepblue66/12384033395/">Ladakh Festival 2013 by Dietmar Temps</a></p>

                <div class="row">
                    <div class = center class="inner cover" style="text-align: left">
                        <h3 style="text-align: left; padding-left: 30"> Threshold</h3>
                        <p style="padding:5 50"> Threshold, which only applies to smart-size mosaics, defines how color-variant a block
                            must be for the algorithm to divide it into more blocks. The lower the threshold, the sharper the mosaic.
                            Notice how the edges in the 500 threshold mosaic are more distinct than those of the 1500 threshold mosaic.
                        </p>
                    </div>

                </div>

                <div class="row">
                    <div class="col-md-4 col-md-push-4">
                        <h4>Original Image </h4>
                        <a href="https://www.flickr.com/photos/deepblue66/12384033395/" target="_blank">
                            <img src="Pictures/Images/Parrot.jpg" class = "img-responsive img-thumbnail">
                        </a>

                    </div>
                    <div class="col-md-4 col-md-pull-4 col-sm-4">
                        <h4>Threshold: 500</h4>
                        <a class = "zoomImage" href="#">
                            <img name = "5" src= "Pictures/Images/Parrot500.jpg" class = "img-responsive img-thumbnail linkImage">
                        </a>
                    </div>
                    <div class="col-md-4 col-sm-4">
                        <h4>Threshold: 1500</h4>
                        <a class = "zoomImage" href="#">
                            <img name ="6" src= "Pictures/Images/Parrot1500.jpg" class = "img-responsive img-thumbnail linkImage">
                        </a>
                    </div>
                </div>
                <p class="center"><a href="https://www.flickr.com/photos/deniwlp84/14451399403/" target="_blank">Blue-and-gold Macaw by Deni Williams</a></p>
            </div></div>



   <form id = "form-id" target = "_blank" action="/getting_started_interactive" method="get" >
       <input type="text" id = "picNum" name = "picNum" style="display:none">
   </form>

    </jsp:attribute>
</t:template>