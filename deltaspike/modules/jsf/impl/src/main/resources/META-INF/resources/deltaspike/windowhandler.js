/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
(function() {
//wrapping so that all internal functions are privately scoped
function isHtml5() {
    try {
        return !!localStorage.getItem;
    } catch(e) {
        return false;
    }
}

// some browsers don't understand JSON - guess which one ... :(
function stringify(someArray) {
    if (JSON) {
        return JSON.stringify(someArray);
    }
    return someArray.join("|||");
}

// store the current body in the html5 localstorage
function storeWindowTree() {
    // first we store all CSS we also need on the intermediate page
    var headNodes = document.getElementsByTagName("head")[0].childNodes;
    var oldSS = new Array();
    var j = 0;
    for (var i = 0; i < headNodes.length; i++) {
        var tagName = headNodes[i].tagName;
        if (tagName && equalsIgnoreCase(tagName, "link") &&
                equalsIgnoreCase(headNodes[i].getAttribute("type"), "text/css")) {

            // sort out media="print" and stuff
            var media = headNodes[i].getAttribute("media");
            if (!media || equalsIgnoreCase(media, "all") || equalsIgnoreCase(media, 'screen')) {
                oldSS[j++] = headNodes[i].getAttribute("href");
            }
        }
    }
    localStorage.setItem(window.name + '_css', stringify(oldSS));
    var body = document.getElementsByTagName("body")[0];
    localStorage.setItem(window.name + '_body', body.innerHTML);
    //X TODO: store ALL attributes of the body tag
    localStorage.setItem(window.name + '_bodyAttrs', body.getAttribute("class"));
    return true;
}

function equalsIgnoreCase(source, destination) {
    //either both are not set or null
    if (!source && !destination) {
        return true;
    }
    //source or dest is set while the other is not
    if (!source || !destination) return false;

    //in any other case we do a strong string comparison
    return source.toLowerCase() === destination.toLowerCase();
}

/** This method will be called onWindowLoad and after AJAX success */
function applyWindowId() {
    if (isHtml5()) { // onClick handling
        var links = document.getElementsByTagName("a");
        for (var i = 0; i < links.length; i++) {
            if (!links[i].onclick) {
                links[i].onclick = function() {storeWindowTree(); return true;};
            } else {
                // prevent double decoration
                if (!("" + links[i].onclick).match(".*storeWindowTree().*")) {
                    //the function wrapper is important otherwise the
                    //last onclick handler would be assigned to oldonclick
                    (function storeEvent() {
                        var oldonclick = links[i].onclick;
                        links[i].onclick = function(evt) {
                            //ie handling added
                            evt = evt || window.event;

                            return storeWindowTree() && oldonclick(evt);
                        };
                    })();
                }
            }
        }
    }
    var forms = document.getElementsByTagName("form");
    for (var i = 0; i < forms.length; i++) {
        var form = forms[i];
        var windowIdHolder = form.elements["dsPostWindowId"];
        if (!windowIdHolder) {
            windowIdHolder = document.createElement("INPUT");
            windowIdHolder.name = "dsPostWindowId";
            windowIdHolder.type = "hidden";
            form.appendChild(windowIdHolder);
        }

        windowIdHolder.value = window.deltaspikeJsWindowId;
    }
}

function getUrlParameter(name) {
    var url = window.location.href;
    var vars = url.split(/&|\?/g);
    for (var i=0; vars != null && i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair[0]==name) {
            return pair[1];
        }
    }
    return null;
}
function setUrlParam(baseUrl, paramName, paramValue) {
    var query = baseUrl;
    var vars = query.split(/&|\?/g);
    var newQuery = "";
    var iParam = 0;
    var paramFound = false;
    for (var i=0; vars != null && i < vars.length; i++) {
        var pair = vars[i].split("=");
        if (pair.length == 1) {
            newQuery = pair[0];
        } else {
            if (pair[0] != paramName) {
                var amp = iParam++ > 0 ? "&" : "?";
                newQuery =  newQuery + amp + pair[0] + "=" + pair[1];
            } else {
                paramFound = true;
                if (paramValue) {
                    var amp = iParam++ > 0 ? "&" : "?";
                    newQuery =  newQuery + amp + paramName + "=" + paramValue;
                }
            }
        }
    }
    if (!paramFound && paramValue) {
        var amp = iParam++ > 0 ? "&" : "?";
        newQuery =  newQuery + amp + paramName + "=" + paramValue;
    }
    return newQuery;
}
// this method runs to ensure that windowIds get checked even if no windowhandler.html is used
function assertWindowId() {
    if (!window.name || window.name.length < 1) {
        url = setUrlParam(window.location.href, 'windowId', null);
        window.name = 'tempWindowId';
        window.location = url;
    }
}

function eraseRequestCookie() {
    var requestToken = getUrlParameter('dsRid'); // random request param
    if (requestToken) {
        var cookieName = 'dsiWindowId-' + requestToken;
        var date = new Date();
        date.setTime(date.getTime()-(10*24*60*60*1000)); // - 10 day
        var expires = "; expires="+date.toGMTString();
        document.cookie = cookieName+"="+expires+"; path=/";
    }
}

var ajaxOnClick = function ajaxDecorateClick(event) {
    if (event.status=="success") {
        applyWindowId();
    }
}

var oldWindowOnLoad = window.onload;

window.onload = function(evt) {
    try {
        (oldWindowOnLoad)? oldWindowOnLoad(evt): null;
    } finally {
        eraseRequestCookie(); // manually erase the old dsRid cookie because Firefox doesn't do it properly
        assertWindowId();
        applyWindowId();
        jsf.ajax.addOnEvent(ajaxOnClick);
    }
}
})();
