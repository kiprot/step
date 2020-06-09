// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.servlets.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {  
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = DatastoreServiceFactory.getDatastoreService().prepare(query);
    ArrayList<Comment> comments = new ArrayList<>();

    int numberOfComments = Integer.parseInt(request.getParameter("num"));
    int tracker = 0;
    
    for(Entity entity : results.asIterable()) {
        // Makes sure to limit comments to the number that user requested.
        if(tracker >= numberOfComments) break;

        long id = entity.getKey().getId();
        long timestamp = (long) entity.getProperty("timestamp");
        String comment = (String) entity.getProperty("comment");
        String imageUrl = (String) entity.getProperty("imageUrl");

        Comment userComment = new Comment(id, comment, timestamp, imageUrl);
        comments.add(userComment);
        tracker++;
    } 
    String jsonComments = new Gson().toJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(jsonComments);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = getUserData(request, "user-comment");
    String name = getUserData(request, "user-name");
    String imageUrl = getUploadedFileUrl(request, "image");

    if(comment == null || name == null) {
      response.setContentType("text/html");
      response.getWriter().println("Please make sure all entries are filled.");
      return;
    }
    String userComment = name + " says " + comment;
    long timestamp = System.currentTimeMillis();
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("comment", userComment);
    commentEntity.setProperty("timestamp", timestamp);

    commentEntity.setProperty("imageUrl", imageUrl);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/dinosaur.html");
  }

  /** Returns user data entry, or null otherwise. */
  private String getUserData(HttpServletRequest request, String attribute) {
    String userData = request.getParameter(attribute);
    if(userData.trim().length() == 0) {
        return null;
    }
    return userData;
  }

  /** Returns a URL that points to the uploaded file, or null if the user did not upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so we get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }  
}
