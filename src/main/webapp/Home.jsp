<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<style>
    .site-wrapper{
        background-color: rgba(50,50,50,0.4);
        color: white;
    }
    footer{
        color:white;
    }
</style>

<t:template isApproved="${isApproved}" log="${log}" page="${page}">
    <jsp:attribute name="scripts">
        <script>
            $(function() {
                $($('footer').find('p')[0]).prepend($('<span>').html("Background: <a href=\"#\" id=\"star\" >The Starry Night</a>;"));
                $("#star").click(function () {

                    document.getElementById('picNum').value = 7;
                    $("#form-id").submit();
                })
            });
        </script>
    </jsp:attribute>
    <jsp:attribute name="content">
        <div class="site-wrapper-inner">

            <div class="cover-container">
                <div id="content" class="inner cover" style="color:white">
                <h1 class="cover-heading">Transform your image into a mosaic of Flickr photos.</h1>
                <p class="lead">This web app breaks your image up into several blocks and replaces each block with the most similar photo
                from our database of Flickr photos. Get started to learn about different options for your mosaic.</p>
                <p class="lead">
                    <a href="getting_started" class="btn btn-lg btn-default">Get Started</a>
                </p>
                </div>
            </div>
        </div>

        <form id = "form-id" target = "_blank" action="/getting_started_interactive" method="get" style = "display: none">
            <input type="text" id = "picNum" name = "picNum">
        </form>
    </jsp:attribute>
</t:template>
