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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.entitybroker.entityprovider.search.Search;


public class MemoryDao {

   private Map<String, MediasiteEntity> storage = new ConcurrentHashMap<String, MediasiteEntity>();

   public void init() {
      // load up a few sample entities
      save( new MediasiteEntity("Test", "This is the test item", "/user/admin", false, 1) );
      save( new MediasiteEntity("SampleTWO", "This is the second preloaded sample item", "/user/admin", false, 10) );
      save( new MediasiteEntity("Thirdsampleitem", "This is the other preloaded sample item ", "/user/admin", true, 5) );
   }

   public MediasiteEntity findById(String id) {
      return storage.get(id);
   }

   public List<MediasiteEntity> findBySearch(Search search) {
      ArrayList<MediasiteEntity> l = new ArrayList<MediasiteEntity>();
      l.addAll(storage.values());
      Collections.sort(l, new EntityComparatorById());
      if (! search.isEmpty()) {
         // restrict based on search param
         if (search.getRestrictionByProperty("title") != null) {
            for (Iterator<MediasiteEntity> iterator = l.iterator(); iterator.hasNext();) {
            	MediasiteEntity we = iterator.next();
               String sMatch = search.getRestrictionByProperty("title").value.toString();
               if (! we.getTitle().contains(sMatch)) {
                  iterator.remove();
               }               
            }
         }
         if (search.getRestrictionByProperty("owner") != null) {
            for (Iterator<MediasiteEntity> iterator = l.iterator(); iterator.hasNext();) {
            	MediasiteEntity we = iterator.next();
               String sMatch = search.getRestrictionByProperty("owner").value.toString();
               if (! we.getOwner().equals(sMatch)) {
                  iterator.remove();
               }               
            }
         }
      }
      return l;
   }

   public void save(MediasiteEntity entity) {
      if (entity == null ||
            entity.getTitle() == null ||
            entity.getOwner() == null ||
            entity.getText() == null) {
         throw new NullPointerException("entity or required fields are null");
      }
      if (entity.getId() != null) {
         if (! storage.containsKey(entity.getId())) {
            entity.setId(null);
         }
      }
      if (entity.getId() == null) {
         entity.setId(getNextId());
      }
      storage.put(entity.getId(), entity);
   }

   public void remove(String id) {
      storage.remove(id);
   }

   public void clear() {
      storage.clear();
   }

   private String getNextId() {
      String newId = null;
      int counter = 0;
      while (newId == null) {
         String id = "id"+counter++;
         if (! storage.containsKey(id)) {
            newId = id;
         }
      }
      return newId;
   }

   public static class EntityComparatorById implements Comparator<MediasiteEntity> {
      public int compare(MediasiteEntity item0, MediasiteEntity item1) {
         return item0.getId().compareTo( item1.getId() );
      }
   }

}