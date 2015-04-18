/**
 * Created by nich on 12/24/14.
 */
$(function () {
    var $body = $("body");
    $(document).ajaxStart(function () {
        $body.addClass("loading");
    });
    $(document).ajaxStop(function () {
        $body.removeClass("loading");
    });
});