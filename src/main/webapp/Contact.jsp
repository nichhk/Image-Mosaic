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
            $(document).ready( function () {
                $("form").submit(function (e) {
                    e.preventDefault(); //STOP default action
                    var inputs = $(this).find('.form-control');
                    var sum = inputs[3].value;
                    console.log(sum);
                    if (sum.localeCompare("7") != 0) {
                        $('#alert').attr("class", "alert alert-danger");
                        $('#alert').html('');
                        $('#alert').append("<span class='glyphicon glyphicon-alert'></span><strong> Error! <span id='error'>You evaluated 4+3 incorrectly.</span></strong>");
                        //document.getElementById("error").innerHTML = "";
                        //document.getElementById("alert-danger").style.visibility = "visible";
                        return;
                    }
                    $.ajax({
                        url: "/contact",
                        type: "POST",
                        data: "inputName="+inputs[0].value+"&inputEmail="+inputs[1].value+"&inputMessage="+inputs[2].value,
                        success: function (data, textStatus, jqXHR) {
                            console.log("great");
                            $('#alert').removeClass('alert alert-danger');
                            $('#alert').addClass('alert alert-success');
                            $('#alert').html("");
                            $('#alert').append("<strong><span class='glyphicon glyphicon-send'></span> Success! Message sent.</strong>");
                        },
                        error: function (jqXHR, textStatus, errorThrown) {
                            console.log("failed");
                        }
                    });
                });
            });
        </script>
    </jsp:attribute>
    <jsp:attribute name="content">
        <div class="site-wrapper-inner">

            <div class="cover-container">
                <!--
                thanks to : http://bootsnipp.com/snippets/featured/bootstrap-3x-contact-form-layout
                -->
                <div id="content" class="inner cover" style="text-align: left">
                    <div class="row">
                        <div class="col-md-12">
                            <div id="alert">
                                </div>
                        </div>
                        <form id="contact" role="form" action="/contact" method="post">
                            <div class="col-sm-12">
                                <div class="well well-sm" style="background-color:#666699"><strong> Required Field</strong> <i class="glyphicon glyphicon-ok form-control-feedback" style="right:11px"></i></div>
                                <div class="form-group">
                                    <label for="InputName">Your Name</label>
                                    <div class="input-group">
                                        <input type="text" class="form-control" name="InputName" id="InputName" placeholder="Enter Name" required>
                                        <span class="input-group-addon"><i class="glyphicon glyphicon-ok form-control-feedback" style="right:-4px"></i></span>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="InputEmail">Your Email</label>
                                    <div class="input-group">
                                        <input type="email" class="form-control" id="InputEmail" name="InputEmail" placeholder="Enter Email" required  >
                                        <span class="input-group-addon"><i class="glyphicon glyphicon-ok form-control-feedback" style="right:-4px"></i></span>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="InputMessage">Message</label>
                                    <div class="input-group">
                                        <textarea name="InputMessage" id="InputMessage" class="form-control" rows="5" required></textarea>
                                        <span class="input-group-addon"><i class="glyphicon glyphicon-ok form-control-feedback" style="right:-4px"></i></span>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="InputReal">What is 4+3?</label>
                                    <div class="input-group">
                                        <input type="text" class="form-control" name="InputReal" id="InputReal" required>
                                        <span class="input-group-addon"><i class="glyphicon glyphicon-ok form-control-feedback" style="right:-4px"></i></span>
                                    </div>
                                </div>
                                <input type="submit" name="submit" id="submit" value="Submit" class="btn btn-info">
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>



    </jsp:attribute>
</t:template>
