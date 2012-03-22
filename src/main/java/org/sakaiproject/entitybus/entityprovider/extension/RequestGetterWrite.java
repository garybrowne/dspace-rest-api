/**
 * $Id: RequestGetterWrite.java 17 2009-02-17 12:33:06Z azeckoski $
 * $URL: http://entitybus.googlecode.com/svn/tags/entitybus-1.0.8/api/src/main/java/org/sakaiproject/entitybus/entityprovider/extension/RequestGetterWrite.java $
 * RequestGetter.java - entity-broker - Apr 8, 2008 8:56:18 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybus.entityprovider.extension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Allows for getting to the request and response objects for the current thread
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RequestGetterWrite extends RequestGetter {

    /**
     * Sets the request for the current thread, this will be cleared when the thread closes
     * @param req the current HttpServletRequest
     */
    public void setRequest(HttpServletRequest req);

    /**
     * Sets the response for the current thread, this will be closed when the thread closes
     * @param res the current HttpServletResponse
     */
    public void setResponse(HttpServletResponse res);

}