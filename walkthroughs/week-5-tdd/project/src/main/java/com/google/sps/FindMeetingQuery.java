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

    if(events.size() > 0) {

        // Adds TimeRanges for events that required attendees will be attending.
        for (Event event : events) {
            for(String attendee : event.getAttendees()) {
                if(attendeesInRequest.contains(attendee)) {
                    eventsTimeRangeList.add(event.getWhen());
                }
            }
        }

        // Adds TimeRanges for events that optional attendees will be attending.
        if(optionalAttendees.size() > 0) {
            for(Event event : events) {
                for(String optionalAttendee : event.getAttendees()) {
                    if(optionalAttendees.contains(optionalAttendee)) {
                        optionalsEventTimeRangeList.add(event.getWhen());
                    }
                }
            }
        }

        // If no required or optional attendees have any other events, return whole day.
        if(eventsTimeRangeList.size() == 0 && optionalsEventTimeRangeList.size() == 0) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }

        // Store available TimeRange slots for optional attendees.
        ArrayList<TimeRange> optionalsSlots = new ArrayList<>();
        if(optionalsEventTimeRangeList.size() > 0) {       
            optionalsSlots = findSlots(optionalsEventTimeRangeList, request);
        }

        // Store available TimeRange slots for required attendees.
        ArrayList<TimeRange> mandatorySlots = new ArrayList<>();
        if(eventsTimeRangeList.size() > 0) {
            mandatorySlots = findSlots(eventsTimeRangeList, request);
        }

        // Finds slots that are common to both requuired and optional attendees and returns them
        // if there are any.
        ArrayList<TimeRange> combinedSlots = new ArrayList<>();
        if(optionalsSlots.size() > 0) {
            for(TimeRange optionalSlot : optionalsSlots) {
                for(TimeRange mandatorySlot : mandatorySlots) {
                    if(mandatorySlot.equals(optionalSlot) || optionalSlot.contains(mandatorySlot)) {
                        combinedSlots.add(mandatorySlot);
                    } else if(mandatorySlot.contains(optionalSlot)) {
                        combinedSlots.add(optionalSlot);
                    }
                }
            }
            if(combinedSlots.size() > 0) return combinedSlots;
        }
        return (mandatorySlots.size() > 0) ? mandatorySlots : optionalsSlots;
    }
    // Returns whole day as option if there are no conflicts.
    return Arrays.asList(TimeRange.WHOLE_DAY);
  }

  // Finds available TimeRange slots for a potential meeting given a meeting request.
  private ArrayList<TimeRange> findSlots(ArrayList<TimeRange> eventsTimeRangeList, MeetingRequest request) {

    ArrayList<TimeRange> timeSlots = new ArrayList<>();
    Collections.sort(eventsTimeRangeList, TimeRange.ORDER_BY_START);

    // Remove nested events.
    for(int i = 0; i < eventsTimeRangeList.size()-1; i++) {
        if(eventsTimeRangeList.get(i).contains(eventsTimeRangeList.get(i+1))) {
            eventsTimeRangeList.remove(eventsTimeRangeList.get(i+1));
        }
    }

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
}