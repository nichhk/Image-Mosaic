<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<style>

    body, .site-wrapper{
        background-image: none !important;
        background-color: white !important;
    }
    #divInfoBox{
        background-color: #eee;
        z-index : 1000;
        display: inline-block;
    }
    #mapId{
        z-index: 0;
    }

    #authAndTitle{
        display: inline-block;
        float: right;
    }
    .info{
        /*width: 100%;*/
        color: black !important;
    }
    #sidebar {
        background-color: #f5f5f5;
        border-left: 1px solid #eee;
        position: fixed;
        right: 0px;
        height: 100%;
        text-align: center;
        overflow-y: scroll;
        z-index: 2000;
    }
    /* source: http://stackoverflow.com/questions/4821724/removing-outline-on-image-map-area */
    img[usemap], map area{
        outline: none;
    }
    footer{
        height: 30px;
    }

    h4:hover{
        cursor: pointer;
        border-radius: 7px 7px 7px 7px;
        -moz-border-radius: 7px 7px 7px 7px;
        -webkit-border-radius: 7px 7px 7px 7px;
        background-color: #85817b;
    }

    @media (min-width: 451px){
        .download{
            position: absolute;
            bottom: 80px;
        }
    }

    @media (max-width: 450px){
        .footer{
            display: none;
        }
        .col-sm-12{
            width: 100% !important;
            height: 65% !important;
        }
        #sidebar{
            position: absolute;
        }
    }


</style>

<t:template isApproved="${isApproved}" log="${log}" action="${action}" page="${page}">

    <jsp:attribute name="scripts">
        <script src="MakeMosaic.min.js"></script>

    </jsp:attribute>

    <jsp:attribute name="content">

            <div class="site-wrapper-inner">

                <div class="cover-container">
                    <div class="inner cover" class="inner cover">
                        <h1>Make a Mosaic</h1>
                        <form id="myForm" class="form-horizontal" action="${action}" method="post" role="form" enctype="multipart/form-data" style="padding-top:5px">

                            <div class="form-group">
                                <label for="userPic" class="col-sm-2 control-label">Your image</label>
                                <div class="col-sm-10">
                                    <input type="file" style="height:2em" id="userPic" name="userPic" accept="image/png, image/gif, image/jpeg" required>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-2 control-label">Smart-sizing</label>
                                <div class="col-sm-10">
                                    <input type="radio" name="smartSizing" checked="checked" value="true">Dynamically-sized blocks
                                    <span>       </span>
                                    <input type="radio" name="smartSizing" value="false">Statically-sized blocks
                                </div>
                            </div>



                            <div class="form-group">
                                <label for="depthIn" class="col-sm-2 control-label">Depth</label>
                                <div class="col-sm-10">
                                    <p>Note: Mosaics of depths 8 or 9 may take up to 10 minutes to complete.</p>
                                    <div><input id="depthIn" type="range" name="depth" min="2" max="9" value="6">
                                        <span id="showDepth"></span>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group" id = "threshSpan">
                                <label for="threshIn" class="col-sm-2 control-label" >Threshold</label>
                                <div class="col-sm-10">
                                    <input id="threshIn" type="range" name="threshold" min="100" max="1500" value="800">
                                    <span id="showThresh"></span>
                                </div>
                            </div>

                            <div><input type="submit" class="btn btn-info" value="Make Mosaic"></div>
                        </form>
                    </div>
                </div>
            </div>

        <div id="mosaicContainer" class="container-fluid" style="display:none">
            <div class="row">
                <div class="col-sm-12 col-sm-10 col-sm-10" style="padding-top: 20px">
                    <div id="picDiv" class ="img-responsive">
                        <!--<img src="#" usemap="#actualmap" class="img-responsive" id="mosaic">-->
                        <map id="mapId" name="actualmap">
                        </map>
                    </div>

                    <div id ="Authors">
                        <a></a>
                    </div>
                </div>

                <div id="sidebar" class="col-sm-12 col-sm-2 col-sm-2 sidebar pull-right">
                    <h4 id="toggleInputs"><i class="glyphicon glyphicon-chevron-up"></i>Your inputs </h4>
                    <div class="dropdownDiv" id="yourInputsDiv">
                        <table class="table" id="yourInputs">
                            <tr>
                                <h5>Image</h5>
                                <img src="#" id="yourImg">
                            </tr>
                        </table>
                    </div>
                    <h4 id="toggleBlock"><i class="glyphicon glyphicon-chevron-up"></i>Block Info</h4>
                    <p id="clickBlockInfo"><i>Click on a block to see its attribution details.</i></p>
                    <div id="blockInfo" class="dropdownDiv">
                    </div>

                    <div class="info download">
                        This mosaic is licensed by <a href="http://creativecommons.org/licenses/by/4.0/">CC BY 4.0</a>.
                        <br>
                    </div>
                </div>
            </div>
        </div>

    </jsp:attribute>
</t:template>