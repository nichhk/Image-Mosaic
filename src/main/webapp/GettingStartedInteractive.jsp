<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>


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
        width: 100%;
        color: black !important;
    }

    /* source: http://stackoverflow.com/questions/4821724/removing-outline-on-image-map-area */
    img[usemap], map area{
        outline: none;
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
    footer{
        height: 30px;
    }
</style>

<t:template isApproved="${isApproved}" log="${log}" page="${page}">

    <jsp:attribute name = "scripts">
        <script>

            $(document).ready(function() {

                var viewWidth = Math.max(document.documentElement.clientWidth, window.innerWidth || 0)*0.85;
                //console.log("size is"+viewWidth);
                var viewHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
                var scale = Math.min(viewWidth/parseInt("${width}"),((viewHeight)/parseInt("${height}")));
                var file = "${filePath}";
                //$("#image").attr('src', file.toString());
                $('<img>').attr({
                    id: "mosaic",
                    src: file,
                    usemap: '#actualmap',
                    id: 'mosaic',
                    width: (parseInt("${width}")*scale).toString(),
                    height: (parseInt("${height}")*scale).toString()
                    //left: xPos.toString()+'%'

                }).prependTo('#picDiv');
                //alert("done");

                //alert("really done");
                var json = "${attributionMap}";
                var images = json.split("()()()()");
                con = 1;
                for(i=0;i<parseInt("${size}");i++) {
                    var data = images[i].split("::::");
                    var area = $('<area>');
                    area.attr({
                        //the id will have the necessary info for #divInfoBox
                        id: data[4],
                        shape: 'rect',
                        coords: (data[0] * scale / con).toString() + ',' + (data[1] * scale / con).toString() + ',' + (data[2] * scale / con).toString() + ',' + (data[3] * scale / con).toString(),
                        href: "#"
                        //target:"_blank"
                    }).appendTo('#mapId');
                    console.log(data[4]);
                }
            });
            function toggleInfoBox() {
                var thisTimer = null;
                var timeoutTime = 100;
                var insidePopup = false;
                var ibox = $("#divInfoBox");
                //set the flag for whether we're in the pop up
                ibox.mouseenter(function(){
                    insidePopup = true;
                }).mouseleave(function(){
                    insidePopup = false;
                    //if we're not in the popup, hide it
                    ibox.hide();
                });

                $('#mapId').on("click", "area", function(){
                    //the info from the id is comma-delimited
                    var info = this.id.split(",");
                    showInfoBox(info[0], info[1], info[2], info[3], info[4], this);
                    return false;

                });
            }


            $(function() {
                toggleInfoBox();
            });

            /*
             This function will add the img, author, and title
             to the #divInfoBox and display it.
             */
            showInfoBox = function(url, author, title, id, trueUrl, area) {
                var img = $('<img>');
                var imgLink = $('<a>').attr({"href":trueUrl});
                $('#zoomedThumb').html("").append(img.attr("src", url));
                $('#title').html(title).attr({"href":trueUrl,"target":'_blank'});
                $('#author').html(author).attr({"href":trueUrl,"target":'_blank'});
                $('#license').html("CC BY-NC 2.0").attr({"href":"http://creativecommons.org/licenses/by-nc/2.0/","target":'_blank'});

            };
        </script>
    </jsp:attribute>

    <jsp:attribute name="content">
            <div id="mosaicContainer" class="container-fluid"  >
                <div class="row">
                    <div class="col-sm-10" style="height: 100%; overflow:scroll; padding-top: 20px">
                        <div id="picDiv" class = "img-responsive">

                            <map id="mapId" name="actualmap">
                            </map>

                        </div>

                        <div id ="Authors">
                            <a></a>
                        </div>
                    </div>

                    <div id="sidebar" class="col-sm-2 sidebar pull-right">
                        <div id="zoomedThumb" class="info"></div>
                        <div class="info" style="color:black">Title: <br><a id="title"></a></div>
                        <div class="info" style="color:black">Author: <br><a id="author"></a></div>
                        <div class="info" style="color:black">License: <br><a id="license"></a></div>

                    </div>
                </div>
            </div>
    </jsp:attribute>
</t:template>
