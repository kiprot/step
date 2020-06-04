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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {  
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    
    ArrayList<Comment> comments = new ArrayList<>();
    for(Entity entity : results.asIterable()) {
        long id = entity.getKey().getId();
        long timestamp = (long) entity.getProperty("timestamp");
        String comment = (String) entity.getProperty("comment");

        Comment userComment = new Comment(id, comment, timestamp);
        comments.add(userComment);
    }
    String jsonComments = new Gson().toJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(jsonComments);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = getUserData(request, "user-comment");
    String name = getUserData(request, "user-name");

    if(comment == null || name == null) {
      response.setContentType("text/html");
      response.getWriter().println("Please make sure both entries are filled.");
      return;
    }
    String userComment = name + " says " + comment;
    long timestamp = System.currentTimeMillis();
    
    Entity taskEntity = new Entity("Comment");
    taskEntity.setProperty("comment", userComment);
    taskEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

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
}
