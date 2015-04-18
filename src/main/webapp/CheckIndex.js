/*
 This function builds the table of crawlerSearches by making a GET request and reading
 the returned JSON
 */
var isApproved;
function getSearches(){
    $.get('crawl', {get : 'crawlerSearches'}, function(responseJson) { //get the crawlerSearches using the crawler servlet
        $.each(responseJson, function(index, crawlerSearch) {  //for each CrawlerSearch object...
            $('<tr>').appendTo('#crawlerSearches')  //make a new row and add it to the table
                .append($('<td class="time">').text(crawlerSearch.time))  //add the time cell
                .append($('<td>').text(crawlerSearch.searchParam))  //add the searchParam cell
                .append($('<td>').text(crawlerSearch.numImg));  //add the numImg cell
        });
    });
}
$(function () {
    isApproved = $('#isApproved').html().localeCompare("1") == 0;
    console.log(isApproved);
    getSearches();
});

/*
 This function allows the user to click on one of the rows in the crawlerSearches table
 and view all of the images corresponding to that search
 */
$(function() {
    $('#crawlerSearches').on('click', 'tr', function () { //when you click a row in the table
        var time = $(this).find('td.time').html();
        $('.alert-success').hide();
        $.ajax({
            url: 'crawl?get=' + time,
            type: "GET",
            success: function (responseJson) {
                $('#picRow').html("");
                if (isApproved) {
                    $('#msg').html("<br><i>Check the images that you want to delete.</i><br>");
                }
                //get the images since time using crawler servlet
                $.each(responseJson, function (index, item) {  //for each image since time...
                    var split = item.split(",");
                    var $img = $('<img>').attr('src', split[0]);  //get the url
                    var $input = $('<input>');  //make a new input for the form
                    $input.attr('type', 'checkbox');
                    $input.attr('name', 'deleteMe');
                    $input.attr('value', item);
                    var col = $('<div>').addClass("col-sm-12 col-md-4 col-lg-2");
                    col.css("padding", "5px");
                    //col.css("text-align", "center");
                    $img.css("padding", "5px");
                    $img.addClass("img-thumbnail");
                    $img.css("height", "130px");
                    $img.css("width", "auto");
                    $input.appendTo(col);
                    $img.appendTo(col);
                    col.appendTo('#picRow');

                    //$('#deleteImgs').append('<br>');
                });
                $('input').hide();
                var $submit = $('<input>');  //make the submit button
                $submit.attr('type', 'submit').addClass('btn btn-info');
                $submit.attr('value', 'Delete');
                if (isApproved) {
                    $submit.appendTo('#deleteImgs');
                }
            }
        });
    });
});

/*
This function allows users to click a thumbnail and thereby check the checkbox
for that image. It overlays an opaque white layer with a delete glyphicon.
Clicking the image again undos what it did before.
 */
$(function() {
    if (isApproved) {
        $('#picRow').on("click", "div", function () {
            var checkbox = $(this).find('input')[0];
            var checked = $(checkbox).is(':checked');
            //if it was already checked
            if (checked == true) {
                //uncheck it
                $(checkbox).prop("checked", false);
                //remove the overlay
                $($(this).find('.overlay')[0]).remove();
            }
            else {
                //check it
                $(checkbox).prop("checked", true);
                var overlay = $('<div>').addClass("overlay");
                var width = $($(this).find('img')[0]).width();
                overlay.width(width + 10);
                overlay.css("left", ($(this).width() - width) / 2);
                overlay.append('<span class="glyphicon glyphicon-remove-circle" style="line-height: 140px">');
                //add the overlay
                $(this).append(overlay);
            }
        });
    }
});

/*
This function submits the form for deleting images from the datastore.
 */
$(function() {
    $("#deleteImgs").submit(function (e) {
        var getData = $(this).serializeArray();
        var numDeleted = getData.length;
        var formURL = $(this).attr("action");
        $.ajax({
            url: formURL,
            type: "POST",
            data: getData,
            success: function (data, textStatus, jqXHR) {
                $('#picRow').empty();
                $('#msg').empty();
                $('.btn-info').remove();
                //show a success bar
                $('#deleteImgs').append($('<div>').addClass('alert alert-success')
                    .append($('<span>').addClass('glyphicon glyphicon-ok'))
                    .append($('<strong>').html("Deleted " + numDeleted + " images.")));
            },
            error: function (jqXHR, textStatus, errorThrown) {
                //if fails
            }
        });
        e.preventDefault(); //STOP default action
    });
});