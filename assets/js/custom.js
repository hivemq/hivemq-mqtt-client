(function (custom, undefined) {

    (function (tabs) {

        var tabGroups;

        tabs.init = function() {
            var cookie = custom.util.readCookie('tabs');
            tabGroups = JSON.parse(cookie) || {};

            var tabHeaders = document.querySelectorAll('.tab');
            for (var i = 0, l = tabHeaders.length; i < l; i++) {
                var tabButtons = tabHeaders[i].querySelectorAll('li > a');
                var tabGroup = tabHeaders[i].getAttribute('data-tab-group');
                for (var j = 0, l2 = tabButtons.length; j < l2; j++) {
                    (function(tabButton, tabGroup, index) {
                        tabs.setActive(tabGroup, 0);
                        addEvent(tabButton, 'click', function(e) {
                            tabs.setActive(tabGroup, index);
                            e.preventDefault();
                            tabButton.scrollIntoView();
                        });
                    })(tabButtons[j], tabGroup, j);
                }
            }

            for (var tabGroup in tabGroups) {
                if (tabGroups.hasOwnProperty(tabGroup)) {
                    tabs.setActive(tabGroup, tabGroups[tabGroup]);
                }
            }
        }

        tabs.setActive = function(str_group, index) {
            var tabHeaders = document.querySelectorAll('.tab[data-tab-group=' + str_group + ']');
            for (var i = 0, l = tabHeaders.length; i < l; i++) {
                var activeTabHeader = tabHeaders[i].getElementsByClassName('tab-active');
                if (activeTabHeader.length != 0) {
                    activeTabHeader[0].classList.remove('tab-active');
                }
                tabHeaders[i].getElementsByTagName('li')[index].classList.add('tab-active');
            }
            var tabContents = document.querySelectorAll('.tab-content[data-tab-group=' + str_group + ']');
            for (var i = 0, l = tabContents.length; i < l; i++) {
                var activeTabContent = tabContents[i].getElementsByClassName('tab-active');
                if (activeTabContent.length != 0) {
                    activeTabContent[0].classList.remove('tab-active');
                }
                tabContents[i].getElementsByTagName('li')[index].classList.add('tab-active');
            }
            tabGroups[str_group] = index;
            var cookie = JSON.stringify(tabGroups);
            custom.util.setCookie('tabs', cookie, 60);
        }

    })(custom.tabs = custom.tabs || {});

    (function (util) {

        util.setCookie = function (str_name, str_value, i_minutes) {
            var str_expires = '';
            if (i_minutes) {
                var date = new Date();
                date.setTime(date.getTime() + (i_minutes * 60 * 1000));
                str_expires = 'expires=' + date.toGMTString() + '; ';
            }
            document.cookie = str_name + '=' + str_value + '; ' + str_expires + 'path=/';
        }

        util.readCookie = function (str_name) {
            var strarray_cookies = document.cookie.split('; ');
            for (var i = 0, l = strarray_cookies.length; i < l; i++) {
                var str_cookie = strarray_cookies[i];
                var strarray_cookieSplit = str_cookie.split('=');
                if (strarray_cookieSplit[0] === str_name) {
                    return strarray_cookieSplit[1];
                }
            }
            return null;
        }

        util.deleteCookie = function (str_name) {
            util.setCookie(str_name, '', -1);
        }

    })(custom.util = custom.util || {});

    function ready() {
        custom.tabs.init();
    }

    // in case the document is already rendered
    if (document.readyState!='loading') ready();
    // modern browsers
    else if (document.addEventListener) document.addEventListener('DOMContentLoaded', ready);
    // IE <= 8
    else document.attachEvent('onreadystatechange', function() {
        if (document.readyState=='complete') ready();
    });

})(window.custom = window.custom || {});