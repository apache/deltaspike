/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
window.dswh = window.dswh || {

    DEBUG_MODE : false,
    TEMP_WINDOW_NAME : 'tempWindowId',
    MANAGED_WINDOW_NAME_PREFIX : 'ds-',

    initialized: false,
    windowId : null,
    clientWindowRenderMode : null,
    maxWindowIdLength : 10,
    cfg : null,

    init : function(windowId, clientWindowRenderMode, maxWindowIdLength, cfg) {

        if (dswh.initialized === true)
        {
            return;
        }

        dswh.initialized = true;

        dswh.utils.log('------- DeltaSpike windowhandler.js -------');
        dswh.utils.log('--- #init(\'' + windowId + '\', \'' + clientWindowRenderMode + '\',' + maxWindowIdLength + ',' + dswh.utils.stringify(cfg) + ')');
        dswh.utils.log('window.name: ' + window.name);

        this.windowId = windowId;
        this.clientWindowRenderMode = clientWindowRenderMode;
        this.maxWindowIdLength = maxWindowIdLength;
        if (cfg) {
            this.cfg = cfg;
        } else {
            this.cfg = {};
        }

        var targetStrategy = this.strategy[clientWindowRenderMode];
        if (targetStrategy) {
            dswh.utils.log('--- #validate');

            targetStrategy.validate();

            // early init
            // this is required if e.g. the onload attr is defined on the body tag and our onload handler won't be called
            // ATTENTION: the ds:windowId component must be placed as last body tag
            dswh.utils.log('--- #init(false)');
            targetStrategy.init(false);

            // JSF ajax callback
            jsf.ajax.addOnEvent(function(event) {
                if (event.status === "success") {
                    dswh.utils.log('--- #init(true)');
                    targetStrategy.init(true);
                }
            });

            // PF ajax callback
            if (window.$ && window.PrimeFaces) {
                $(document).on('pfAjaxComplete', function () {
                    dswh.utils.log('--- #init(true)');
                    targetStrategy.init(true);
                });
            }

            // init via onload
            // just as fallback if ds:windowId is not placed at last body tag
            var oldWindowOnLoad = window.onload;
            window.onload = function(evt) {
                try {
                    (oldWindowOnLoad) ? oldWindowOnLoad(evt) : null;
                } finally {
                    dswh.utils.log('--- #init(false)');
                    targetStrategy.init(false);
                }
            };
        }
    },

    strategy : {

        'CLIENTWINDOW' : {

            validate : function() {
                this.cleanupCookies();
                this.assertWindowId();
            },

            init : function(ajax) {
                this.overwriteLinkOnClickEvents();
                this.overwriteButtonOnClickEvents();

                dswh.utils.appendHiddenWindowIdToForms();
            },

            assertWindowId : function() {
                dswh.utils.log('--- #assertWindowId');

                // ensure that windowIds get checked even if no windowhandler.html is used
                if (!dswh.utils.isWindowNameDefined() || !dswh.utils.isManagedWindowName()) {
                    dswh.utils.log('window name not defined or unmanaged - request new windowId');
                    dswh.utils.requestNewWindowId();
                }
            },

            overwriteLinkOnClickEvents : function() {

                var tokenizedRedirectEnabled = dswh.cfg.tokenizedRedirect;
                var storeWindowTreeEnabled = dswh.utils.isHtml5() && dswh.cfg.storeWindowTreeOnLinkClick;

                dswh.utils.log('--- #overwriteLinkOnClickEvents');
                dswh.utils.log('tokenizedRedirect: ' + dswh.cfg.tokenizedRedirect);
                dswh.utils.log('storeWindowTreeOnLinkClick: ' + dswh.cfg.storeWindowTreeOnLinkClick);

                if (tokenizedRedirectEnabled || storeWindowTreeEnabled) {
                    var links = document.getElementsByTagName("a");
                    for (var i = 0; i < links.length; i++) {
                        var link = links[i];

                        var target = link.getAttribute('target');

                        if (dswh.strategy.CLIENTWINDOW.isHrefDefined(link) === true && (!target || target === '_self')) {
                            if (!link.onclick) {
                                link.onclick = function(evt) {
                                    // IE handling added
                                    evt = evt || window.event;

                                    // skip open in new tab
                                    if (!evt.ctrlKey) {
                                        if (storeWindowTreeEnabled) {
                                            dswh.strategy.CLIENTWINDOW.storeWindowTree();
                                        }
                                        if (tokenizedRedirectEnabled) {
                                            dswh.strategy.CLIENTWINDOW.tokenizedRedirect(this);
                                            return false;
                                        }

                                        return true;
                                    }
                                };
                            } else {
                                // prevent double decoration
                                if (!("" + link.onclick).match(".*storeWindowTree().*")) {
                                    //the function wrapper is important otherwise the
                                    //last onclick handler would be assigned to oldonclick
                                    (function storeEvent() {
                                        var oldonclick = link.onclick;
                                        link.onclick = function(evt) {
                                            // IE handling added
                                            evt = evt || window.event;

                                            var proceed = oldonclick.bind(this)(evt);
                                            if (typeof proceed === 'undefined' || proceed === true) {

                                                // skip open in new tab
                                                if (!evt.ctrlKey) {
                                                    if (storeWindowTreeEnabled) {
                                                        dswh.strategy.CLIENTWINDOW.storeWindowTree();
                                                    }

                                                    if (tokenizedRedirectEnabled) {
                                                        dswh.strategy.CLIENTWINDOW.tokenizedRedirect(this);
                                                        return false;
                                                    }
                                                }
                                            }
                                            return proceed;
                                        };
                                    })();
                                }
                            }
                        }
                    }
                }
            },

            overwriteButtonOnClickEvents : function() {

                var storeWindowTreeEnabled = dswh.utils.isHtml5() && dswh.cfg.storeWindowTreeOnButtonClick;

                dswh.utils.log('--- #overwriteButtonOnClickEvents');
                dswh.utils.log('storeWindowTreeOnButtonClick: ' + dswh.cfg.storeWindowTreeOnButtonClick);

                if (storeWindowTreeEnabled) {
                    var inputs = document.getElementsByTagName("input");
                    for (var i = 0; i < inputs.length; i++) {
                        var input = inputs[i];
                        if (input.getAttribute("type") === "submit" || input.getAttribute("type") === "button") {
                            if (!input.onclick) {
                                input.onclick = function() {
                                    dswh.strategy.CLIENTWINDOW.storeWindowTree();
                                    return true;
                                };
                            } else {
                                // prevent double decoration
                                if (!("" + input.onclick).match(".*storeWindowTree().*")) {
                                    //the function wrapper is important otherwise the
                                    //last onclick handler would be assigned to oldonclick
                                    (function storeEvent() {
                                        var oldonclick = input.onclick;
                                        input.onclick = function(evt) {
                                            //ie handling added
                                            evt = evt || window.event;

                                            dswh.strategy.CLIENTWINDOW.storeWindowTree();

                                            return oldonclick.bind(this)(evt);
                                        };
                                    })();
                                }
                            }
                        }
                    }
                }
            },

            isHrefDefined : function(link) {

                var href = link.getAttribute("href");

                if (!href || href === null) {
                    return false;
                }

                // trim
                href = href.replace(/^\s+|\s+$/g, '');

                if (href === '') {
                    return false;
                }

                if (href.indexOf('#') === 0) {
                    return false;
                }

                if (href.lastIndexOf('javascript:', 0) === 0) {
                    return false;
                }

                return true;
            },

            tokenizedRedirect : function(link) {

                dswh.utils.log('--- #tokenizedRedirect');

                var requestToken = dswh.utils.generateNewRequestToken();
                dswh.utils.storeCookie('dsrwid-' + requestToken, dswh.windowId, 3);
                window.location = dswh.utils.setUrlParam(link.href, 'dsrid', requestToken);
            },

            /**
             * store the current body in the html5 localstorage
             */
            storeWindowTree : function() {

                dswh.utils.log('--- #storeWindowTree');

                // first we store all CSS we also need on the intermediate page
                var headNodes = document.getElementsByTagName("head")[0].childNodes;
                var oldSS = new Array();
                var j = 0;
                for (var i = 0; i < headNodes.length; i++) {
                    var tagName = headNodes[i].tagName;
                    if (tagName
                            && dswh.utils.equalsIgnoreCase(tagName, "link")
                            && dswh.utils.equalsIgnoreCase(headNodes[i].getAttribute("type"), "text/css")) {

                        // sort out media="print" and stuff
                        var media = headNodes[i].getAttribute("media");
                        if (!media
                                || dswh.utils.equalsIgnoreCase(media, "all")
                                || dswh.utils.equalsIgnoreCase(media, 'screen')) {
                            oldSS[j++] = headNodes[i].getAttribute("href");
                        }
                    }
                }
                localStorage.setItem(window.name + '_css', dswh.utils.stringify(oldSS));

                var body = document.getElementsByTagName("body")[0];
                localStorage.setItem(window.name + '_body', body.innerHTML);

                var attributes = {};
                for (var i = 0; i < body.attributes.length; i++) {
                    var attribute = body.attributes[i];
                    attributes[attribute.name] = attribute.value;
                }
                localStorage.setItem(window.name + '_bodyAttributes', dswh.utils.stringify(attributes));

                var scrollTop = (window.pageYOffset || document.documentElement.scrollTop) - (document.documentElement.clientTop || 0);
                localStorage.setItem(window.name + '_scrollTop', scrollTop);

                var scrollLeft = (window.pageXOffset || document.documentElement.scrollLeft) - (document.documentElement.clientLeft || 0);
                localStorage.setItem(window.name + '_scrollLeft', scrollLeft);
            },

            cleanupCookies : function() {
                dswh.utils.log('--- #cleanupCookies');

                var dsrid = dswh.utils.getUrlParameter(window.location.href, 'dsrid');
                if (dsrid) {
                    dswh.utils.expireCookie('dsrwid-' + dsrid);
                }
            }
        },

        'LAZY' : {

            validate : function() {
                this.cleanupCookies();
                this.assertWindowId();
            },

            init : function(ajax) {
                dswh.utils.appendHiddenWindowIdToForms();
            },

            assertWindowId : function() {
                var dswid = dswh.utils.getUrlParameter(window.location.href, 'dswid');

                dswh.utils.log('--- #assertWindowId');
                dswh.utils.log('dswid: ' + dswid);

                // window name is defined -> existing tab
                if (dswh.utils.isWindowNameDefined()) {

                    // is the current window name a already managed by DS?
                    if (dswh.utils.isManagedWindowName()) {

                        var windowId = dswh.utils.getWindowIdFromWindowName();

                        // we triggered the windowId recreation last request
                        if (windowId === dswh.TEMP_WINDOW_NAME) {
                            // enabled initial redirect
                            // -> use the new windowId from the url
                            if (dswid) {
                                dswh.utils.log('assign window name from request parameter');

                                dswh.utils.setWindowIdAsWindowName(dswid);
                            }
                            // disabled initial redirect
                            // -> use the new windowId from the rendered config as no url param is available
                            else {
                                dswh.utils.log('assign window name from server windowId');

                                dswh.utils.setWindowIdAsWindowName(dswh.windowId);
                            }
                        }
                        // security check like on the server side
                        else if (windowId.length > dswh.maxWindowIdLength) {
                            dswh.utils.log('window id from window name exeeds maxWindowIdLength - request new windowId');

                            dswh.utils.requestNewWindowId();
                        }
                        // window name doesn't match requested windowId
                        // -> redirect to the same view with current windowId from the window name
                        else if (windowId !== dswid) {
                            dswh.utils.log('reload url with window name');

                            window.location = dswh.utils.setUrlParam(window.location.href, 'dswid', windowId);
                        }
                    }
                    else {
                        dswh.utils.log('window name is unmanaged - request new windowId');

                        dswh.utils.requestNewWindowId();
                    }
                }
                // window name is undefined -> "open in new tab/window" was used
                else {
                    // url param available?
                    if (dswid) {
                        // initial redirect
                        // -> the windowId is valid - we don't need to a second request
                        if (dswh.cfg.initialRedirectWindowId && dswid === dswh.cfg.initialRedirectWindowId) {
                            dswh.utils.log('assign window name from initialRedirectWindowId');

                            dswh.utils.setWindowIdAsWindowName(dswh.cfg.initialRedirectWindowId);
                        }
                        // != initial redirect
                        // -> request a new windowId to avoid multiple tabs with the same windowId
                        else {
                            dswh.utils.log('request new windowId');

                            dswh.utils.requestNewWindowId();
                        }
                    }
                    // as no url parameter is available, the request is a new tab with disabled initial redirect
                    // -> just use the windowId from the renderer
                    else if (dswh.windowId) {
                        dswh.utils.log('assign window name from server windowId');

                        dswh.utils.setWindowIdAsWindowName(dswh.windowId);
                    }
                }
            },

            cleanupCookies : function() {
                dswh.utils.log('--- #cleanupCookies');

                var dswid = dswh.utils.getUrlParameter(window.location.href, 'dswid');
                if (dswid) {
                    dswh.utils.expireCookie('dsrwid-' + dswid);
                }
            }
        }
    },

    utils : {

        findRootWindow: function() {
            var w = window;
            while(w.frameElement) {
                var parent = w.parent;
                if (parent === undefined) {
                    break;
                }
                w = parent;
            };

            return w;
        },

        isWindowNameDefined : function() {
            var w = dswh.utils.findRootWindow();
            return w.name && w.name.length > 0;
        },

        isManagedWindowName : function() {
            var w = dswh.utils.findRootWindow();
            if (!w.name) {
                return false;
            }

            return w.name.indexOf(dswh.MANAGED_WINDOW_NAME_PREFIX) === 0;
        },

        getWindowIdFromWindowName : function() {
            return dswh.utils.findRootWindow().name.substring(dswh.MANAGED_WINDOW_NAME_PREFIX.length);
        },

        setWindowIdAsWindowName : function(windowId) {
            dswh.utils.findRootWindow().name = dswh.MANAGED_WINDOW_NAME_PREFIX + windowId;
        },

        requestNewWindowId : function() {
            // set temp window name to remember the current state
            dswh.utils.setWindowIdAsWindowName(dswh.TEMP_WINDOW_NAME);

            // we remove the dswid if available and redirect to the same url again to create a new windowId
            window.location = dswh.utils.setUrlParam(window.location.href, 'dswid', null);

            // set temp window name to remember the current state (again - sometimes required for IE!?)
            dswh.utils.setWindowIdAsWindowName(dswh.TEMP_WINDOW_NAME);
        },

        isHtml5 : function() {
            try {
                return !!localStorage.getItem;
            } catch(e) {
                return false;
            }
        },

        stringify : function(someArray) {
            // some browsers don't understand JSON - guess which one ... :(
            if (JSON) {
                return JSON.stringify(someArray);
            }
            return someArray.join("|||");
        },

        unstringify : function(serialized) {
            if (JSON) {
                return JSON.parse(serialized);
            }

            return serialized.split("|||");
        },

        equalsIgnoreCase : function(source, destination) {
            //either both are not set or null
            if (!source && !destination) {
                return true;
            }
            //source or dest is set while the other is not
            if (!source || !destination) return false;

            //in any other case we do a strong string comparison
            return source.toLowerCase() === destination.toLowerCase();
        },

        getUrlParameter : function(uri, name) {
            // create an anchor object with the uri and let the browser parse it
            var a = document.createElement('a');
            a.href = uri;

            // check if a query string is available
            var queryString = a.search;
            if (queryString && queryString.length > 0) {
                // create an array of query parameters - substring(1) removes the ? at the beginning of the query
                var queryParameters = queryString.substring(1).split("&");
                for (var i = 0; i < queryParameters.length; i++) {
                    var queryParameter = queryParameters[i].split("=");
                    if (queryParameter[0] === name) {
                        return queryParameter.length > 1 ? decodeURIComponent(queryParameter[1]) : "";
                    }
                }
            }

            return null;
        },

        setUrlParam : function(uri, parameterName, parameterValue) {
            var a = document.createElement('a');
            a.href = uri;

            // set empty string as value if not defined or empty
            if (!parameterValue || parameterValue.replace(/^\s+|\s+$/g, '').length === 0) {
                parameterValue = '';
            }

            // check if value is empty
            if (parameterValue.length === 0) {

                // both value and query string is empty (or doesn't contain the param), don't touch the url
                if (a.search.length === 0 || a.search.indexOf(parameterName + "=") === -1) {
                    return a.href;
                }
            }

            // query string is empty, just append our new parameter
            if (a.search.length === 0) {
                a.search = '?' + encodeURIComponent(parameterName) + "=" + encodeURIComponent(parameterValue);

                return a.href;
            }

            var oldParameters = a.search.substring(1).split('&');
            var newParameters = [];
            newParameters.push(parameterName + "=" + encodeURIComponent(parameterValue));

            // loop old parameters, remove empty ones and remove the parameter with the same name as the new one
            for (var i = 0; i < oldParameters.length; i++) {
                var oldParameterPair = oldParameters[i];

                if (oldParameterPair.length > 0) {
                    var oldParameterName = oldParameterPair.split('=')[0];
                    var oldParameterValue = oldParameterPair.split('=')[1];

                    // don't add empty parameters again
                    if (oldParameterValue && oldParameterValue.replace(/^\s+|\s+$/g, '').length > 0) {
                        // skip the the old parameter if it's the same as the new parameter
                        if (oldParameterName !== parameterName) {
                            newParameters.push(oldParameterName + "=" + oldParameterValue);
                        }
                    }
                }
            }

            // join new parameters
            a.search = '?' + newParameters.join('&');

            return a.href;
        },

        appendHiddenWindowIdToForms : function() {
            var forms = document.getElementsByTagName("form");
            for (var i = 0; i < forms.length; i++) {
                var form = forms[i];
                var dspwid = form.elements["dspwid"];
                if (!dspwid) {
                    dspwid = document.createElement("INPUT");
                    dspwid.setAttribute("name", "dspwid");
                    dspwid.setAttribute("type", "hidden");
                    form.appendChild(dspwid);
                }

                dspwid.setAttribute("value", dswh.windowId);
            }
        },

        expireCookie : function(cookieName) {
            var date = new Date();
            date.setTime(date.getTime()-(10*24*60*60*1000)); // - 10 day
            var expires = ";max-age=0;expires=" + date.toGMTString();

            document.cookie = cookieName + "=" + expires + "; path=/";
        },

        generateNewRequestToken : function() {
            return "" + Math.floor(Math.random() * 999);
        },

        generateNewWindowId : function() {
            return "" + Math.floor((Math.random() * (9999 - 1000)) + 1000);
        },

        storeCookie : function(name, value, seconds) {
            var expiresDate = new Date();
            expiresDate.setTime(expiresDate.getTime() + (seconds * 1000));
            var expires = "; expires=" + expiresDate.toGMTString();

            document.cookie = name + '=' + value + expires + "; path=/";
        },

        log : function(message) {
            if (dswh.DEBUG_MODE === true) {
                console.log(message);
            }
        }
    }
};

// required for IE8
if (!Function.prototype.bind) {
    Function.prototype.bind = function (oThis) {
        if (typeof this !== 'function') {
            // closest thing possible to the ECMAScript 5
            // internal IsCallable function
            throw new TypeError('Function.prototype.bind - what is trying to be bound is not callable');
        }

        var aArgs = Array.prototype.slice.call(arguments, 1),
                fToBind = this,
                fNOP = function () {
                },
                fBound = function () {
                    return fToBind.apply(this instanceof fNOP && oThis
                            ? this
                            : oThis,
                            aArgs.concat(Array.prototype.slice.call(arguments)));
                };

        fNOP.prototype = this.prototype;
        fBound.prototype = new fNOP();

        return fBound;
    };
}