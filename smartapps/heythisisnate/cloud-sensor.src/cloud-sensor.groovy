/**
 *  NodeMCU Cloud Sensor App
 *
 *  Copyright 2017 Nate Clark | @heythisisnate
 *  
 *  A desccription of this project and documentaion can be found at:
 *  https://github.com/heythisisnate/nodemcu-smartthings-sensors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
  name: "Cloud Sensor",
  namespace: "heythisisnate",
  author: "Nate Clark",
  description: "A webhook handler for internet connected contact or motion sensors",
  category: "Safety & Security",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity@2x.png"
)

preferences {
	section("Select devices to monitor") {
  	input "contactSensors", "capability.contactSensor", multiple:true, required:false
    input "motionSensors", "capability.motionSensor", multiple:true, required:false
	}
}

mappings {
  path("/event") {
    action: [
      POST: "handle_event"
    ]
  }
}

def handle_event() {
  def event = request.JSON
  def sensor_id = event.sensor_id 
  def allSensors = contactSensors + motionSensors
  def device = allSensors.find { 
    sensor_id == it.id
  }
  
  if(device == null)
    httpError(501, "Unknown device " + sensor_id)
  
  switch (event.state) {
    case 0: device.close(); break;
    case 1: device.open(); break;
    default: httpError(500, "Unknown device state " + event.state);
  }

  log.trace "Updated " + device + " to " + event.state
  
  return [ "success": true ]
}