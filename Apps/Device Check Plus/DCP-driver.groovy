/**
 *  ****************  Device Check Plus Driver  ****************
 *
 *  Design Usage:
 *  This driver works with the Device Check Plus app.
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
 * 
 * ------------------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V1.0.0 - 03/31/20 - Initial release
 */

def setVersion(){
    appName = "DeviceCheckPlusDriver"
	version = "v1.0.0" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def update() {
    log.info "In update"
    setVersion()
    if(logEnable) runIn(1800,logEnable)
}

metadata {
	definition (name: "Device Check Plus Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Device%20Check%20Plus/DCP-driver.groovy") {
        capability "Switch"

        attribute "switch", "string"
		
        attribute "dwDriverInfo", "string"
        command "update"	
	}
	preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Device Check Plus</b>", description: "This device was created by Device Check Plus<br>"
            input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

def on() {
    if(logEnable) log.info "In Device Check Plus Driver - Turning Switch On"
    device.on
    sendEvent(name: "switch", value: "on", displayed: true)
    runIn(1,off)
}

def off() {
    if(logEnable) log.info "In Device Check Plus Driver - Turning Switch Off"
    device.off
    sendEvent(name: "switch", value: "off", displayed: true)
} 
