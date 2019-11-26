(function (jtd, undefined) {
(function (custom, undefined) {

  (function (tabs) {

    var tabGroups;

    tabs.init = function() {
      var cookie = custom.util.readCookie('tabs');
      tabGroups = JSON.parse(cookie) || {};

      var tabHeaders = document.querySelectorAll('.tab');
      for (var i = 0, l = tabHeaders.length; i < l; i++) {

        var tabGroup = tabHeaders[i].getAttribute('data-tab-group');
        tabs.setActive(tabGroup, tabGroups[tabGroup] || 0);

        var tabButtons = tabHeaders[i].querySelectorAll('li > a');
        for (var j = 0, l2 = tabButtons.length; j < l2; j++) {
          (function(tabButton, tabGroup, index) {
            jtd.addEvent(tabButton, 'click', function(e) {
              var beforeTop = tabButton.getBoundingClientRect().top;
              tabs.setActive(tabGroup, index);
              e.preventDefault();
              var afterTop = tabButton.getBoundingClientRect().top;
              var scrollParent = custom.util.getScrollParent(tabButton);
              if (scrollParent != null) {
                scrollParent.scrollTop += afterTop - beforeTop;
              }
            });
          })(tabButtons[j], tabGroup, j);
        }
      }

      var hash = window.location.hash.substring(1);
      window.location.hash = ""
      window.location.hash = hash
    }

    tabs.setActive = function(str_group, index) {
      var tabHeaders = document.querySelectorAll('.tab[data-tab-group=' + str_group + ']');
      for (var i = 0, l = tabHeaders.length; i < l; i++) {
        var activeTabHeader = custom.util.queryChildren(tabHeaders[i], 'li.tab-active');
        if (activeTabHeader.length != 0) {
          activeTabHeader[0].classList.remove('tab-active');
        }
        custom.util.queryChildren(tabHeaders[i], 'li')[index].classList.add('tab-active');
      }
      var tabContents = document.querySelectorAll('.tab-content[data-tab-group=' + str_group + ']');
      for (var i = 0, l = tabContents.length; i < l; i++) {
        var activeTabContent = custom.util.queryChildren(tabContents[i], 'li.tab-active');
        if (activeTabContent.length != 0) {
          activeTabContent[0].classList.remove('tab-active');
        }
        custom.util.queryChildren(tabContents[i], 'li')[index].classList.add('tab-active');
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

    var queryChildrenCount = 0;
    util.queryChildren = function(element, str_selector) {
      var id = element.id;
      if (!id) {
        element.id = 'query_children_' + queryChildrenCount++;
      }
      var result = element.parentNode.querySelectorAll('#' + element.id + ' > ' + str_selector);
      if (!id) {
        element.removeAttribute('id');
      }
      return result;
    }

    util.getScrollParent = function (node) {
      if (node == null) {
        return null;
      }
      if (node.scrollTop != undefined) {
        if (node.scrollTop != 0) {
          return node;
        }
        node.scrollTop = 1;
        if (node.scrollTop == 1) {
          node.scrollTop = 0;
          return node;
        }
      }
      return custom.util.getScrollParent(node.parentNode);
    }

  })(custom.util = custom.util || {});

  jtd.onReady(function() {
    custom.tabs.init();
  });

})(jtd.custom = jtd.custom || {});
})(window.jtd = window.jtd || {});