/**
 * $Id: EntityLastModified.java 17 2009-02-17 12:33:06Z azeckoski $
 * $URL: http://entitybus.googlecode.com/svn/tags/entitybus-1.0.8/api/src/main/java/org/sakaiproject/entitybus/entityprovider/annotations/EntityLastModified.java $
 * EntityId.java - entity-broker - Apr 13, 2008 12:17:49 PM - azeckoski
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

package org.sakaiproject.entitybus.entityprovider.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;

/**
 * Marks a getter method or field as the last modified time code (unix time code) for an entity,
 * this can be a {@link Date}, {@link Long}, long, or {@link String} (will attempt to convert this to a long)<br/>
 * the convention is to try to convert the return from the 
 * "getLastModified" method or the value in the "lastModified" field<br/>
 * <b>NOTE:</b> This annotation should only be used once in a class,
 * the getter method must take no arguments and return an object
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface EntityLastModified { }
