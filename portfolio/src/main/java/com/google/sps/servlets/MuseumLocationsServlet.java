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

import com.google.sps.data.MuseumLocations;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Returns museum location as a JSON array */
@WebServlet("/museum-locations")
public class MuseumLocationsServlet extends HttpServlet {
    
    private Collection<MuseumLocations> museumLocations;

    @Override
    public void init() {
        museumLocations = new ArrayList<>();
        Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/museums.csv"));
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] cells = line.split(",");

            String name = cells[0];
            Double lat = Double.parseDouble(cells[1]);
            Double lng = Double.parseDouble(cells[2]);
            int layer = Integer.parseInt(cells[3]);

            museumLocations.add(new MuseumLocations(name, lat, lng, layer));
        }
        scanner.close();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse respose) throws IOException {
        respose.setContentType("application/json");
        String json = new Gson().toJson(museumLocations);
        respose.getWriter().println(json);
    }
}