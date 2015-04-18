/*
*
*
*
* These functions below are for handling the form
*
*
 */

/*
These two functions display the value of the slider
when it's first displayed and when it changes
 */
$(function() {
    $('#showThresh').html($('#threshIn').val());
    $('#threshIn').change( function() {
        $('#showThresh').html($('#threshIn').val());
    });
});

$(function() {
    $('#showDepth').html($('#depthIn').val());
    $('#depthIn').change(function () {
        $('#showDepth').html($('#depthIn').val());
    });
});

/*
 This function is necessary because the max value of the depth slider
 depends on whether the mosaic uses smart-sizing.
 */
$(function(){
    //when the user clicks the other radio button
    $("input[name=smartSizing]").change(function() {
        //dynam is true is smart-sizing is checked; false other wise
        var dynam = $('input[name="smartSizing"]:checked').val().localeCompare("true") == 0;
        if(dynam){
            //max depth is 8 for smart-sizing
            $("#depthIn").attr("max",9);
            //$("#threshSpan").show();
            $("#threshIn").attr("disabled",false);
            //$("#showThresh").show();
            //increase the depth to 8 if it was previously 7, the max for statically-sized
            if ($('#depthIn').val() == 8){
                $('#showDepth').html("9");
                $('#depthIn').val(9);
            }
        }
        else{
            //the max depth is 7 for statically-sized blocks
            $("#depthIn").attr("max",8);
            //if it was 8 before, change it to 7
            if ($('#depthIn').val() == 9){
                $('#depthIn').val(8);
                $('#showDepth').html("8");
            }
            //threshold is not necessary for statically-sized blocks
            $("#threshIn").attr("disabled",true);
        }
    });
});


var depth, smartSizing, thresh, image, time;


/*
This method displays the mosaic given the response JSON
 */
function displayMosaic(resp) {
    $('.site-wrapper-inner').hide();
    $('#mosaicContainer').show();
    //make an img and set its src, height, and width from resp

    var viewWidth = Math.max(document.documentElement.clientWidth, window.innerWidth || 0) * 0.85;
    //console.log("size is"+viewWidth);
    var viewHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
    var scale = Math.min(viewWidth / resp.width, viewHeight / resp.height);
    console.log("scale is" + scale);

    $('<img>').attr({
        id: "mosaic",
        src: resp.url,
        usemap: '#actualmap',
        width: (resp.width * scale).toString(),
        height: (resp.height * scale).toString()
    }).prependTo('#picDiv');
    //iterate over the attriution table
    for (var i = 0; i < resp.attributionTable.length; i++) {
        var attribute = resp.attributionTable[i];
        //make an area, set its attributes, make it of class thumbnail, and add it to the mapId
        var area = $('<area>');
        var x1 = Math.round(parseInt(attribute.x1) * scale);
        var x2 = Math.round(parseInt(attribute.x2) * scale);
        var y1 = Math.round(parseInt(attribute.y1) * scale);
        var y2 = Math.round(parseInt(attribute.y2) * scale);

        //var x2 = parseFloat(attribute.x2)*xScale;
        area.attr({
            //the id will have the necessary info for #divInfoBox
            id: attribute.url + "," + attribute.author + "," + attribute.title
                + "," + attribute.id + "," + attribute.trueUrl + "," + attribute.license,
            shape: 'rect',
            coords: x1.toString() + ',' + y1.toString() + ',' + x2.toString() + ',' + y2.toString(),
            href: "#"
            //target:"_blank"
        }).appendTo('#mapId');
        //console.log(x1+","+y1+";"+x2+","+y2);
    }
    //make a link to download the image
    var link = $('<a>', {
            text: "Download Mosaic",
            download: "Collage.png",
            href: "http://storage.googleapis.com/image-mosaic.appspot.com/" + time + "_mosaic"
        }
    ).addClass('btn btn-info text-center');
    $('.info').append(link);
    //set the user's inputs in the sidebar
    var glyph = "<i class='glyphicon glyphicon-remove'></i>";
    if (smartSizing) {
        glyph = "<i class='glyphicon glyphicon-ok'></i>"
    }
    //show the pic they uploaded
    readURL(userPic);
    //set the smartsizing and depth and thresh if it exists
    $('#yourInputs').append($('<tr>').append($('<p>').html("<h5>" + glyph + " Smart-sizing</h5>")))
        .append($('<tr>').append($('<p>').html("<h5>Depth: " + depth + "</h5>")));
    if (smartSizing) {
        $('#yourInputs').append($('<tr>').append($('<p>').html("<h5>Threshold: " + thresh + "</h5>")));
    }
}

/*
 This function gets the mosaic, if it exists
 */

function checkIsDone(){
    $.ajax({
        url: "/get_mosaic",
        type: 'GET',
        data: "time=" + time,
        processData: false,
        contentType: false,
        success: function (resp) {
            //if the servlet returns "not done", continue
            if (typeof resp === 'string') {
            }
            //otherwise, that means it's sending the mosaic
            else {
                //cancel the timer
                clearInterval(myInterval);
                //display it
                displayMosaic(resp);
                //and we're done
                $('body').removeClass('loading');
            }
        }
    });
}
/*
source: http://stackoverflow.com/questions/1542280/do-something-every-5-seconds-and-the-code-to-stop-it-jquery
 */
var iFrequency = 7500; // expressed in miliseconds
var myInterval = 0;

// STARTS and Resets the loop if any
function startCheck() {
    $('body').addClass('loading');
    myInterval = setInterval( "checkIsDone()", iFrequency );  // run
}

/*
 This script submits the form the build a collage with ajax.
 Upon success, it will display the collage and layer it's
 attribution map on top.
 */

$(function () {
    $( '#myForm' ).submit( function( e ) {
        e.preventDefault();
        time = (new Date()).getTime();
        $('#myForm').prepend($('<input>').attr({'type':'text', 'name':'time', 'value': time}).css("display", "none"));
        //get the action url
        var formURL = $(this).attr("action");
        var userPic = document.getElementById('userPic');
        depth = document.getElementById('depthIn').value;
        smartSizing = $('input[name="smartSizing"]:checked').val().localeCompare("true") == 0;
        thresh = null;
        if (smartSizing) {
            thresh = document.getElementById('threshIn').value;
        }
        //POST the form to upload the data
        $.ajax({
            url: formURL,
            type: 'POST',
            data: new FormData(this),
            dataType: 'json',
            processData: false,
            contentType: false
            //upon successful completion of the form
        });
        startCheck();
    });
});
/*
*
*
*
* These functions below are for displaying info about blocks when they are clicked
*
*
*
 */

/*
This function will show the block info when an area is clicked.
 */
$(function() {
    $('#mapId').on("click", "area", function(){
        //the info from the id is comma-delimited
        var info = this.id.split(",");
        showInfoBox(info[0], info[1], info[2], info[3], info[4], info[5], this);
        return false;
    });
});

/*
 This function set the links and thumbnail in the sidebar
 */

showInfoBox = function(url, author, title, id, trueUrl, license, area) {
    hide($('#yourInputsDiv'), $('#toggleInputs'));
    show($('#blockInfo'), $('#toggleBlock'));
    $('#clickBlockInfo').remove();
    $('#blockInfo').empty();
    $('#blockInfo').append($('<img>').attr('src', url));
    $('#blockInfo').append($('<h5>').html("Title<br>").append($('<a>').attr({'href':trueUrl, 'target':'_blank'}).html(title)));
    $('#blockInfo').append($('<h5>').html("Author<br>").
        append($('<a>').attr({'href':"https://www.flickr.com/photos/" +  id, "target":'_blank'}).html(author)));
    makeLicenseLink(license);
    $('#blockInfo').append($('<p>').html("This image has been scaled in the mosaic."));
};




/*
Source: http://stackoverflow.com/questions/5802580/html-input-type-file-get-the-image-before-submitting-the-form
Given an file input element, it sets the file as the src for #yourImg
 */
function readURL(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function (e) {
            $('#yourImg')
                .attr('src', e.target.result)
                .addClass('img-responsive');
        };
        reader.readAsDataURL(input.files[0]);
    }
}

/*
This function sets the link to a CC license given the
license's number
 */
function makeLicenseLink(license){
    var numLicense = 0;
    numLicense = Number(license);
    var name = "";
    var href = "";
    /*All of these names and hrefs are given here:
     https://www.flickr.com/services/api/flickr.photos.licenses.getInfo.html
     */
    if (numLicense == 1){
        name = "CC BY-NC-SA 2.0";
        href = "http://creativecommons.org/licenses/by-nc-sa/2.0/";
    }
    else if (numLicense == 2){
        name = "CC BY-NC 2.0";
        href = "http://creativecommons.org/licenses/by-nc/2.0/";
    }
    else if (numLicense == 4){
        name = "CC BY 2.0";
        href = "http://creativecommons.org/licenses/by/2.0/";
    }
    else if (numLicense == 5){
        name = "CC BY-SA 2.0";
        href = "http://creativecommons.org/licenses/by-sa/2.0/";
    }
    else if (numLicense == 7){
        name = "No known copyright restrictions";
        href = "http://flickr.com/commons/usage/";
    }
    $('#blockInfo').append($('<h5>').html("License<br>").append($('<a>').attr({'href':href, 'target': '_blank'}).html(name)));
}



/*
hide(which, toggle) hides the jquery element hide and toggles the glyphicon of toggle from - to +
 */
function hide(which, toggle){
    //if which is already hidden, don't do anything
     if (which.is(":hidden")){
         return;
     }
    which.slideUp("fast");
    $(toggle.find('i')[0]).removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
}

/*
 hide(which, toggle) shows the jquery element hide and toggles the glyphicon of toggle from + to -
 */

function show(which, toggle){
    //if which is hidden,
    if (which.is(":hidden")){
        //show which
        which.slideDown("fast");
        //switch the sign for toggle
        $(toggle.find('i')[0]).removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
        return;
    }
    //otherwise, don't do anything
}

/*
This function makes sure that only one of the info divs is shown
It also allows users to click on one of the headers to open/close it
 */
$(function () {
    $('#toggleInputs').on('click', function () {
        //if your inputs are hidden, show it
        if ($('#yourInputsDiv').is(":hidden")){
            show($('#yourInputsDiv'), $('#toggleInputs'));
            hide($('#blockInfo'), $('#toggleBlock'));
        }
        //otherwise, hide it
        else{
            hide($('#yourInputsDiv'), $('#toggleInputs'));
            show($('#blockInfo'), $('#toggleBlock'));
        }
    });
    $('#toggleBlock').on('click', function() {
        //if block info is hidden, show it
        if ($('#blockInfo').is(":hidden")){
            show($('#blockInfo'), $('#toggleBlock'));
            hide($('#yourInputsDiv'), $('#toggleInputs'));
        }
        //otherwise, hide it
        else{
            hide($('#blockInfo'), $('#toggleBlock'));
            show($('#yourInputsDiv'), $('#toggleInputs'));
        }
    })
});

