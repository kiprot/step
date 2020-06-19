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

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class FindMeetingQuery {

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> attendeesInRequest = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    ArrayList<TimeRange> eventsTimeRangeList = new ArrayList<>();
    ArrayList<TimeRange> optionalsEventTimeRangeList = new ArrayList<>();
    
    // Returns no options if duration is longer than a day.
    if(request.getDuration() >  TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();
    }
    
    // Adds TimeRanges for events that required attendees and optional attendees will be attending.
    for (Event event : events) {
        for(String attendee : event.getAttendees()) {
            if(attendeesInRequest.contains(attendee)) {
                eventsTimeRangeList.add(event.getWhen());
            }
            if(optionalAttendees.contains(attendee)) {
                optionalsEventTimeRangeList.add(event.getWhen());
            }
        }
    }

    // If no required or optional attendees have any other events, return whole day.
    if(eventsTimeRangeList.size() == 0 && optionalsEventTimeRangeList.size() == 0) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // Combine both optional and mandatory attendees and treat them as 'mandatory' so that we
    // can check if there are any slots available for both grooups to attend
    ArrayList<TimeRange> optionalAndMandatory = new ArrayList<>();
    optionalAndMandatory.addAll(eventsTimeRangeList);
    optionalAndMandatory.addAll(optionalsEventTimeRangeList);
    Collections.sort(optionalAndMandatory, TimeRange.ORDER_BY_START);
    ArrayList<TimeRange> slotsForBothGroups = findSlots(optionalAndMandatory, request);

    // Return list of TimeRanges for slots that both groups can attend, if there are any. Else,
    // proceed onwards and process slots for mandatory attendees only.
    if(slotsForBothGroups.size() != 0) {
        return slotsForBothGroups;
    }    

    // Store available TimeRange slots for required attendees, and return them if there are any.
    ArrayList<TimeRange> mandatorySlots = new ArrayList<>();
    if(eventsTimeRangeList.size() > 0) {
        mandatorySlots = findSlots(eventsTimeRangeList, request);
    }
    if(mandatorySlots.size() > 0) return mandatorySlots;

    // Store available TimeRange slots for optional attendees, and return them if there are any.
    // If there are none, this function returns an empty list.
    ArrayList<TimeRange> optionalsSlots = new ArrayList<>();
    if(optionalsEventTimeRangeList.size() > 0) {       
        optionalsSlots = findSlots(optionalsEventTimeRangeList, request);
    }
    return optionalsSlots;
  }

  // Finds available TimeRange slots for a potential meeting given a meeting request.
  private ArrayList<TimeRange> findSlots(ArrayList<TimeRange> timeRangeList, MeetingRequest request) {
    ArrayList<TimeRange> timeSlots = new ArrayList<>();
    ArrayList<TimeRange> eventsTimeRangeList = mergeNestedOrOverlappingEvnets(timeRangeList);

    // Add START_OF_DAY slot if the first TimeRange is not START_OF_DAY.
    if(eventsTimeRangeList.get(0).start() != TimeRange.START_OF_DAY) {
        timeSlots.add( TimeRange.fromStartEnd(TimeRange.START_OF_DAY, eventsTimeRangeList.get(0)
            .start(), false) );
    }

    for(int i = 0; i < eventsTimeRangeList.size()-1; i++) {
        int startTime = eventsTimeRangeList.get(i).end();
        int endTime   = eventsTimeRangeList.get(i+1).start();
        long timeBetweenEvents = (long) (endTime - startTime);

        // Checks if there is time overlaps, and if there is enough time to last the given meeting duration.
        if( (startTime < endTime) && (timeBetweenEvents >= request.getDuration()) ) {
            timeSlots.add( TimeRange.fromStartEnd(startTime, endTime, false) ); 
        }        
    }

    // Add last TimeRange if the last TimeRange is not END_OF_DAY inclusive.
    if( (timeSlots.size() > 0) && (eventsTimeRangeList.get(eventsTimeRangeList.size()-1).end() != TimeRange.END_OF_DAY+1) ) {
        timeSlots.add( TimeRange.fromStartEnd(eventsTimeRangeList.get(eventsTimeRangeList.size()-1).end(),
            TimeRange.END_OF_DAY, true) );
    }
    return timeSlots;
  }

  // Merges nested and overlapping events into one longer event.
  public ArrayList<TimeRange> mergeNestedOrOverlappingEvnets(ArrayList<TimeRange> allUnavailableTimes) {
    Collections.sort(allUnavailableTimes, TimeRange.ORDER_BY_START);

    ArrayList<TimeRange> result = new ArrayList<>();
    for (TimeRange currentTime: allUnavailableTimes) {
      
      // Add this TimeRange if it does not overlap with the previous one.
      if (result.isEmpty() || !currentTime.overlaps(result.get(result.size()-1))) {
        result.add(currentTime);
      } else {  
        TimeRange lastTimeRangeInResult = result.get(result.size()-1);
        TimeRange mergedTimeRange = TimeRange.fromStartEnd(
            Math.min(lastTimeRangeInResult.start(), currentTime.start()),
            Math.max(lastTimeRangeInResult.end(), currentTime.end()),
            false
        );
        result.remove(lastTimeRangeInResult);
        result.add(mergedTimeRange);
      }
    }
    return result;
  }
}
