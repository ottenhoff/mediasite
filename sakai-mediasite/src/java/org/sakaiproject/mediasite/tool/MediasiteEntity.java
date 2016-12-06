/*
 * Copyright 2016 Sonic Foundry, Inc. Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/ECL-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.sakaiproject.mediasite.tool;

import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;

public class MediasiteEntity {

   @EntityId
   private String id;
   private String title;
   private String text;
   private String owner;
   private boolean flagged;
   private int number;
   
   public MediasiteEntity() {}

   public MediasiteEntity(String title, String text, String owner) {
      this.title = title;
      this.text = text;
      this.owner = owner;
   }

   public MediasiteEntity(String title, String text, String owner, boolean flagged, int number) {
      this.title = title;
      this.text = text;
      this.owner = owner;
      this.flagged = flagged;
      this.number = number;
   }

   @Override
   public boolean equals(Object obj) {
      if (null == obj)
         return false;
      if (!(obj instanceof MediasiteEntity))
         return false;
      else {
         MediasiteEntity castObj = (MediasiteEntity) obj;
         if (null == this.id || null == castObj.id)
            return false;
         else
            return (this.id.equals(castObj.id));
      }
   }

   @Override
   public int hashCode() {
      if (null == this.id)
         return super.hashCode();
      String hashStr = this.getClass().getName() + ":" + this.id.hashCode();
      return hashStr.hashCode();
   }

   @Override
   public String toString() {
      return "id:" + this.id + ", stuff:" + this.title + ", number:" + number;
   }

   public String getId() {
      return id;
   }
   
   public void setId(String id) {
      this.id = id;
   }
   
   public String getTitle() {
      return title;
   }
   
   public void setTitle(String title) {
      this.title = title;
   }
   
   public String getText() {
      return text;
   }
   
   public void setText(String text) {
      this.text = text;
   }
   
   public boolean isFlagged() {
      return flagged;
   }
   
   public void setFlagged(boolean flagged) {
      this.flagged = flagged;
   }
   
   public int getNumber() {
      return number;
   }
   
   public void setNumber(int number) {
      this.number = number;
   }

   public String getOwner() {
      return owner;
   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

}