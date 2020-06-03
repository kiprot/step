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

//added Gson dependency
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  ArrayList<String> sampleMessages = new ArrayList<>();


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {  
    String messages = convertToJsonString(sampleMessages);
    response.setContentType("text/html;");
    response.getWriter().println(messages);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the inpiut from the form.
    String userComment = getUserComment(request);
    if(userComment == null) {
        response.setContentType("text/html");
        response.getWriter().println("Please enter a valid comment");
        return;
    }
    sampleMessages.add(userComment);
    response.sendRedirect("/dinosaur.html");
  }

  private String getUserComment(HttpServletRequest request) {
      // Get input from the form.
      String userName = request.getParameter("user-name");
      String userComment = request.getParameter("user-comment");

      if(userName.length() <= 0) {
          System.err.println("Please input name");
          return null;
      }
      if(userComment.length() <= 0) {
          System.err.println("No Comment");
          return null;
      }
      return userName + " says " + userComment;
  }

  public String convertToJsonString(ArrayList<String> list) {
    return new Gson().toJson(list);
  }

}
