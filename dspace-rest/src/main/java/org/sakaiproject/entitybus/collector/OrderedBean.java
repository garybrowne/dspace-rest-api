/**
 * $Id: OrderedBean.java 17 2009-02-17 12:33:06Z azeckoski $
 * $URL: http://entitybus.googlecode.com/svn/tags/entitybus-1.0.8/api/src/main/java/org/sakaiproject/entitybus/collector/OrderedBean.java $
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008 Sakai Foundation
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
 **/

package org.sakaiproject.entitybus.collector;

/**
 * This sets the order of a bean when it is grabbed by the bean collector
 * and ensures a guaranteed priority order of collected beans,
 * lower orders (numbers) will be loaded first
 * and the orders do not have to be consecutive (there can be gaps)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface OrderedBean extends AutoRegister {

   /**
    * Sets the order to load the bean which implements this method compared
    * to other beans of the same type, lower orders (numbers) will be loaded first
    * (i.e. order 1 will appear before order 3 in the list) and the 
    * orders do not have to be consecutive (there can be gaps), 
    * 2 beans with the same order or beans with no order set will be ordered randomly
    * @return an int which represents the loading order
    */
   public int getOrder();

}