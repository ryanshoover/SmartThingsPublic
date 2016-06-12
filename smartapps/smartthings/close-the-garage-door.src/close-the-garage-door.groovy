/**
 *  Copyright 2015 SmartThings
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
 *  Garage Door Monitor
 *
 *  Author: SmartThings
 */
definition(
    name: "Close the Garage Door!",
    namespace: "smartthings",
    author: "ryanshoover",
    description: "Monitor your garage door and get a text message if it is open too long",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)

preferences {
	section("When the garage door is open...") {
		input "multisensor", "capability.threeAxis", title: "Which?"
	}
	section("For how long...") {
		input "maxOpenTime", "number", title: "Minutes?"
	}
}

def installed() {
	subscribe(multisensor, "acceleration", accelerationHandler)
}

def updated() {
	unsubscribe()
	subscribe(multisensor, "acceleration", accelerationHandler)
}

def accelerationHandler(evt) {
	def latestThreeAxisState = multisensor.threeAxisState // e.g.: 0,0,-1000

	if (latestThreeAxisState) {
		def isOpen = Math.abs(latestThreeAxisState.xyzValue.x) > 500 // TODO: Test that this value works in most cases...

		if (!isOpen) {
			state.status = "closed"
		}

		if (isOpen) {
			state.status = "open"
			runIn(maxOpenTime * 60, takeAction, [overwrite: true])
		}
	}
	else {
		log.warn "COULD NOT FIND LATEST 3-AXIS STATE FOR: ${multisensor}"
	}
}

def takeAction(){
	if (state.status == "open") {
		sendTextMessage()
	}
}

def sendTextMessage() {
	log.debug "$multisensor was open too long, texting $phone"

	def openMinutes = maxOpenTime
	def msg = "Your ${multisensor.label ?: multisensor.name} has been open for more than ${openMinutes} minutes!"
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
		sendPush msg
    }
}
