<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<link href="../../stylesheets/CrawlerSearches.css" rel="stylesheet">

<style>
    body, .site-wrapper{
        background-image: none !important;
        background-color: white !important;
    }
    @media (min-width: 768px){
        .site-wrapper-inner {
            vertical-align: top !important;
        }
    }
    .overlay{
        position:   absolute;
        z-index:    1000;
        top:        0;
        /*left:       0;*/
        height:     100%;
        /*width:      100%;*/
        background: rgba( 255, 255, 255, .8 );
    }

</style>

<t:template isApproved="${isApproved}" log="${log}" page="${page}">
    <jsp:attribute name="scripts">
        <script src="CheckIndex.min.js">
        </script>
        <script>
        $(function () {
            var $body = $("body");
            $(document).ajaxStart(function () {
                $body.addClass("loading");
            });
            $(document).ajaxStop(function () {
                $body.removeClass("loading");
            });
        });
        </script>

    </jsp:attribute>


    <jsp:attribute name="content">
        <div class="container-fluid" style="padding-left: 20px; padding-right: 20px">
            <div class="crawlerSearches" style="padding-top: 10px">
                <p id="isApproved" style="display:none">${isApproved}</p>
                <p><i>Click on one of the rows below to see the images from that search</i></p>
                <table id="crawlerSearches" class="table table-hover">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Search parameter</th>
                            <th>Number of images</th>
                        </tr>
                    </thead>

                </table>
            </div>

            <form id="deleteImgs" action="/crawl" method="GET">
                    <p id="msg"></p>
                    <div class="row" id="picRow">
                    </div>
            </form>
        </div>
    </jsp:attribute>



</t:template>