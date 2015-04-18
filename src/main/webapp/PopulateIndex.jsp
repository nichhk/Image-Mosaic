<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link href="../../stylesheets/LoadingGif.css" rel="stylesheet">
<style>
    .site-wrapper{
        background-color: rgba(50,50,50,0.4);
        color: white;
    }
    footer{
        color:white;
    }
</style>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:template isApproved="${isApproved}" log="${log}" page="${page}">
    <jsp:attribute name="scripts">
        <script>
            /*
            This script submits the form.
             */
            $(function() {
                $('#alert').hide();
                $("#theForm").submit(function (e) {
                    $('#alert').hide();
                    e.preventDefault(); //STOP default action
                    var inputs = $(this).find('.form-control');
                    $.ajax({
                        url: "/crawl",
                        type: "GET",
                        data: "searchParam="+inputs[0].value+"&howMany="+inputs[1].value,
                        success: function (data, textStatus, jqXHR) {
                            //show the success alert on success
                            $('#alert').show();
                        },
                        error: function (jqXHR, textStatus, errorThrown) {
                            console.log("failed");
                        }
                    });
                });
            });
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
        <div class="site-wrapper-inner">

            <div class="cover-container">
                <div class="inner cover">
                    <h1>Populate Index</h1>
                    <div class="col-sm-12">
                        <div id="alert" class="alert alert-success">
                            <span class='glyphicon glyphicon-floppy-saved'></span><strong> Download complete</strong>
                        </div>
                        <form id = "theForm" action="/crawl" method="GET" role="form">
                            <div class="form-group">
                                <label for="searchParam">What should we search Flickr for?</label>
                                <input class="form-control" type="text" name="searchParam" id="searchParam" required>
                            </div>
                            <div class="form-group">
                                <label for="howMany">How many images should we download?</label>
                                <div><input class="form-control" type="number" name="howMany" id="howMany" min="1" max="100" required></div>
                            </div>
                        <div><input id="submit" type="submit" class="btn btn-info" value="Download photos"></div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </jsp:attribute>

</t:template>