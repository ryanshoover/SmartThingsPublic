/**
 *  HomeBridge
 *
 *  Copyright 2016 Ryan Hoover
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
    name: "HomeBridge",
    namespace: "ryanshoover",
    author: "Ryan Hoover",
    description: "Creates a bridge between HomeKit and SmartThings\r\nhttps://github.com/jnewland/homebridge",
    category: "Convenience",
    iconUrl: "http://i0.wp.com/hoover.ws/wp-content/uploads/sites/3/2016/06/topic-homekit.png",
    iconX2Url: "http://i0.wp.com/hoover.ws/wp-content/uploads/sites/3/2016/06/topic-homekit.png",
    iconX3Url: "http://i0.wp.com/hoover.ws/wp-content/uploads/sites/3/2016/06/topic-homekit.png",
    oauth: true)

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    if (!state.accessToken) {
        createAccessToken()
    }
}

preferences {
    page(name: "copyConfig")
}

def copyConfig() {
    dynamicPage(name: "copyConfig", title: "Config", install:true) {
        section() {
            paragraph "Copy/Paste the below into your homebridge's config.json to create HomeKit accessories for your Hello Home actions"
            href url:"https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/config?access_token=${state.accessToken}", style:"embedded", required:false, title:"Config", description:"Tap, select, copy, then click \"Done\""
        }
    }
}

def renderConfig() {
    def configJson = new groovy.json.JsonOutput().toJson(location?.helloHome?.getPhrases().collect({
        [
            accessory: "SmartThingsHelloHome",
            name: it.label,
            appId: it.id,
            accessToken: state.accessToken
        ]
    }))

    def configString = new groovy.json.JsonOutput().prettyPrint(configJson)
    render contentType: "text/plain", data: configString
}

mappings {
    if (!params.access_token || (params.access_token && params.access_token != state.accessToken)) {
        path("/config") { action: [GET: "authError"] }
    } else {
        path("/config") { action: [GET: "renderConfig"]  }
    }
}

def authError() {
    [error: "Permission denied"]
}
