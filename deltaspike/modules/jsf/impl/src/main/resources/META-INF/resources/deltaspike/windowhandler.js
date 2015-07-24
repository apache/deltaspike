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
window.dswh = window.dswh || {

    windowId : null,
    clientWindowRenderMode : null,
    cfg: null,

    init : function(windowId, clientWindowRenderMode, cfg) {
        this.windowId = windowId;
        this.clientWindowRenderMode = clientWindowRenderMode;
        if (cfg) {
            this.cfg = cfg;
        } else {
            this.cfg = {};
        }

        var targetStrategy = this.strategy[clientWindowRenderMode];
        if (targetStrategy) {
            targetStrategy.validate();

            // early init
            // this is required if e.g. the onload attr is defined on the body tag and our onload handler won't be called
            // ATTENTION: the ds:windowId component must be placed as last body tag
            targetStrategy.init(false);

            // JSF ajax callback
            jsf.ajax.addOnEvent(function(event) {
                if (event.status === "success") {
                    targetStrategy.init(true);
                }
            });

            // PF ajax callback
            if (window.$ && window.PrimeFaces) {
                $(document).on('pfAjaxComplete', function () {
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
                this.overwriteOnClickEvents();

                dswh.utils.appendHiddenWindowIdToForms();
            },

            assertWindowId : function() {
                // ensure that windowIds get checked even if no windowhandler.html is used
                if (!window.name || window.name.length < 1) {
                    window.name = 'tempWindowId';
                    window.location = dswh.utils.setUrlParam(window.location.href, 'dswid', null);
                }
            },

            overwriteOnClickEvents : function() {

                var tokenizedRedirectEnabled = dswh.cfg.tokenizedRedirect;
                var storeWindowTreeEnabled = dswh.utils.isHtml5() && dswh.cfg.storeWindowTree;

                if (dswh.cfg.tokenizedRedirect || storeWindowTreeEnabled) {
                    var links = document.getElementsByTagName("a");
                    for (var i = 0; i < links.length; i++) {
                        var link = links[i];

                        if (storeWindowTreeEnabled) {
                            if (!link.onclick) {
                                link.onclick = function() {
                                    dswh.strategy.CLIENTWINDOW.storeWindowTree();
                                    return true;
                                };
                            } else {
                                // prevent double decoration
                                if (!("" + link.onclick).match(".*storeWindowTree().*")) {
                                    //the function wrapper is important otherwise the
                                    //last onclick handler would be assigned to oldonclick
                                    (function storeEvent() {
                                        var oldonclick = link.onclick;
                                        link.onclick = function(evt) {
                                            //ie handling added
                                            evt = evt || window.event;

                                            return dswh.strategy.CLIENTWINDOW.storeWindowTree() && oldonclick.bind(this)(evt);
                                        };
                                    })();
                                }
                            }
                        }

                        if (tokenizedRedirectEnabled && dswh.strategy.CLIENTWINDOW.tokenizedRedirectRequired(link) === true) {
                            if (!link.onclick) {
                                link.onclick = function() {
                                    dswh.strategy.CLIENTWINDOW.tokenizedRedirect(this);
                                    return false;
                                };
                            } else {
                                // prevent double decoration
                                if (!("" + link.onclick).match(".*tokenizedRedirect.*")) {
                                    //the function wrapper is important otherwise the
                                    //last onclick handler would be assigned to oldonclick
                                    (function storeEvent() {
                                        var oldonclick = link.onclick;
                                        link.onclick = function(evt) {
                                            //ie handling added
                                            evt = evt || window.event;

                                            var proceed = oldonclick.bind(this)(evt);
                                            if (typeof proceed === 'undefined' || proceed === true) {
                                                dswh.strategy.CLIENTWINDOW.tokenizedRedirect(this);
                                                return false;
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

            tokenizedRedirectRequired : function(link) {
                // skip link without href
                if (link.href && link.href.length > 0) {
                    return true;
                }

                return false;
            },

            tokenizedRedirect : function(link) {
                var requestToken = dswh.utils.generateRequestToken();
                dswh.utils.storeCookie('dsrwid-' + requestToken, dswh.windowId, 3);
                window.location = dswh.utils.setUrlParam(link.href, 'dsrid', requestToken);
            },

            /**
             * store the current body in the html5 localstorage
             */
            storeWindowTree : function() {
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
                //X TODO: store ALL attributes of the body tag
                localStorage.setItem(window.name + '_bodyAttrs', body.getAttribute("class"));
                return true;
            },

            cleanupCookies : function() {
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

                // window.name is null which means that "open in new tab/window" was used
                if (!window.name || window.name.length < 1) {

                    // url param available?
                    if (dswid) {
                        // initial redirect case
                        // the windowId is valid - we don't need to a second request
                        if (dswh.cfg.initialRedirectWindowId && dswid === dswh.cfg.initialRedirectWindowId) {
                            window.name = dswh.cfg.initialRedirectWindowId;
                        }
                        else {
                            // -- url param available, we must recreate a new windowId to be sure that it is new and valid --

                            // set tempWindowId to remember the current state
                            window.name = 'tempWindowId';
                            // we remove the dswid if available and redirect to the same url again to create a new windowId
                            window.location = dswh.utils.setUrlParam(window.location.href, 'dswid', null);
                        }
                    }
                    else if (dswh.windowId) {
                        // -- no dswid in the url -> an initial request without initial redirect --

                        // this means that the initial redirect is disabled and we can just use the windowId as window.name
                        window.name = dswh.windowId;
                    }
                }
                else {
                    if (window.name === 'tempWindowId') {
                        // we triggered the windowId recreation last request - use it now!
                        window.name = dswid;
                    }
                    else if (window.name !== dswid) {
                        // window.name doesn't match requested windowId
                        // -> redirect to the same view with current window.name / windowId
                        window.location = dswh.utils.setUrlParam(window.location.href, 'dswid', window.name);
                    }
                }
            },

            cleanupCookies : function() {
                var dswid = dswh.utils.getUrlParameter(window.location.href, 'dswid');
                if (dswid) {
                    dswh.utils.expireCookie('dsrwid-' + dswid);
                }
            }
        }
    },

    utils : {

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

        getUrlParameter : function (uri, name) {
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
                        return queryParameter.length > 1 ? queryParameter[1] : "";
                    }
                }
            }

            return null;
        },

        setUrlParam : function (baseUrl, paramName, paramValue) {
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
        },

        appendHiddenWindowIdToForms : function() {
            var forms = document.getElementsByTagName("form");
            for (var i = 0; i < forms.length; i++) {
                var form = forms[i];
                var dspwid = form.elements["dspwid"];
                if (!dspwid) {
                    dspwid = document.createElement("INPUT");
                    dspwid.name = "dspwid";
                    dspwid.type = "hidden";
                    form.appendChild(dspwid);
                }

                dspwid.value = dswh.windowId;
            }
        },

        expireCookie : function(cookieName) {
            var date = new Date();
            date.setTime(date.getTime()-(10*24*60*60*1000)); // - 10 day
            var expires = ";max-age=0;expires=" + date.toGMTString();

            document.cookie = cookieName + "=" + expires + "; path=/";
        },

        generateRequestToken : function() {
            return Math.floor(Math.random() * 999);
        },

        storeCookie : function(name, value, seconds) {
            var expiresDate = new Date();
            expiresDate.setTime(expiresDate.getTime() + (seconds * 1000));
            var expires = "; expires=" + expiresDate.toGMTString();

            document.cookie = name + '=' + value + expires + "; path=/";
        }
    }
};
