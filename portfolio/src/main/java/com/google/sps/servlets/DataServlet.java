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

  // adding hard-coded messages to arraylist
  ArrayList<String> sampleMessages = new ArrayList<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {  
    String messages = convertToJsonString(sampleMessages);
    response.setContentType("text/html;");
    response.getWriter().println(messages);
  }

  public String convertToJsonString(ArrayList<String> list) {
    sampleMessages.add("The MET is the best -ken");
    sampleMessages.add("I love dinosaurs -john");
    sampleMessages.add("The T_Rex looks cool -mary");
    sampleMessages.add("New York Museums are great -jane");
    return new Gson().toJson(list);
  }

}
