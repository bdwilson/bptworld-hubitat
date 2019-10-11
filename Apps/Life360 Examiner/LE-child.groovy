/**
 *  ****************  Life360 Examiner Child App  ****************
 *
 *  Design Usage:
 *  Simple app to log the raw data coming over from the Life360 phone app.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
 * 
 *-------------------------------------------------------------------------------------------------------------------
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V1.0.0 - 10/09/19 - Initial release.
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "Life360ExaminerChildVersion"
	state.version = "v1.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "Life360 Examiner Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Simple app to log the raw data coming over from the Life360 phone app.",
    category: "Convenience",
    parent: "BPTWorld:Life360 Examiner",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page(name: "pageConfig")
    page(name: "pageData", title: "", install: false, uninstall: true, nextPage: "pageConfig")

}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 Examiner</h2>", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Simple app to log the raw data coming over from the Life360 phone app."
            paragraph "* MUST use the 'Life360 with States' app"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
			input "presenceDevice", "capability.presenceSensor", title: "Select 'Life360 with States' User Device", required: true
            input "historyHourType", "bool", title: "Time Selection for History Tile (Off for 24h, On for 12h)", defaultValue: false, submitOnChange:true
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Report")) {
            href "pageData", title:" Raw Data Report", description:"Click Here"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "false", title: "Enable Debug Logging", description: "Debugging", submitOnChange:true)
		}
		display2()
	}
}

def pageData() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Life360 Examiner Data</h2>") {
        section(getFormat("header-green", "${getImage("Blank")}"+" Life360 Data - ${presenceDevice}")) {
            if(state.history) {
                paragraph "${state.history}"
            } else {
                paragraph "No data to display."
            }
		}
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	updated()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
	initialize()
}

def initialize() {
    setDefaults()
    if(presenceDevice) subscribe(presenceDevice, "address1", userHandler)
    
    if(parent.awDevice) schedule("0 0 3 ? * * *", setVersion)
}

def userHandler(evt) {
    if(logEnable) log.debug "---------- Start Log - Life360 Examiner - App version: ${state.version} ----------"
    state.address1 = presenceDevice.currentValue("address1")
    state.latitude = presenceDevice.currentValue("latitude")
    state.longitude = presenceDevice.currentValue("longitude")
    state.battery = presenceDevice.currentValue("battery")
    
    getDateTime()
    theMap = "https://www.google.com/maps/search/?api=1&query=${state.latitude},${state.longitude}"
    
    try {
        getDateTime()
        msgValue = "<tr><td>${newDate}</td><td><a href='${theMap}' target='_blank'>${state.address1}</a></td><td>${state.latitude}, ${state.longitude}</td><td>${state.battery}</td></tr>"
        
        if(logEnable) log.trace "In userHandler - new - msgValue: <table>${msgValue}</table>"
        
        if(state.list == null) state.list = []
        state.list.add(0,msgValue)  

        listSize = state.list.size()
        if(listSize > 20) state.list.removeAt(20)

        String result = state.list.join(";")
        
        def lines = result.split(";")
        numOfLines = state.list.size()
        if(logEnable) log.trace "In userHandler - numOfLines: ${numOfLines}"
        logTop20 = "<table width='100%'>"
        logTop20 += "<tr><td><b>Date</b></td><td><b>Address1</b></td><td><b>Latitude, Longitude</b></td><td><b>Battery</b></td></tr>"
        
        if(numOfLines >= 1) logTop20 += "${lines[0]}"
        if(numOfLines >= 2) logTop20 += "${lines[1]}"
        if(numOfLines >= 3) logTop20 += "${lines[2]}"
        if(numOfLines >= 4) logTop20 += "${lines[3]}"
        if(numOfLines >= 5) logTop20 += "${lines[4]}"
        if(numOfLines >= 6) logTop20 += "${lines[5]}"
        if(numOfLines >= 7) logTop20 += "${lines[6]}"
        if(numOfLines >= 8) logTop20 += "${lines[7]}"
        if(numOfLines >= 9) logTop20 += "${lines[8]}"
        if(numOfLines >= 10) logTop20 += "${lines[9]}"
        if(numOfLines >= 11) logTop20 += "${lines[10]}"
        if(numOfLines >= 12) logTop20 += "${lines[11]}"
        if(numOfLines >= 13) logTop20 += "${lines[12]}"
        if(numOfLines >= 14) logTop20 += "${lines[13]}"
        if(numOfLines >= 15) logTop20 += "${lines[14]}"
        if(numOfLines >= 16) logTop20 += "${lines[15]}"
        if(numOfLines >= 17) logTop20 += "${lines[16]}"
        if(numOfLines >= 18) logTop20 += "${lines[17]}"
        if(numOfLines >= 19) logTop20 += "${lines[18]}"
        if(numOfLines >= 20) logTop20 += "${lines[19]}"
        
        logTop20 += "</table>"
    
        state.history = logTop20
    }
    catch(e1) {
        log.warn "Life360 Examiner - sendHistory - Something went wrong<br>${e1}"        
    }
    if(logEnable) log.debug "---------- End Log - Life360 Examiner - App version: ${state.version} ----------"
}

def getDateTime() {
	def date = new Date()
	if(historyHourType == false) newDate=date.format("E HH:mm")
	if(historyHourType == true) newDate=date.format("E hh:mm a")
    return newDate
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){			// Modified from @Stephack Code
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Life360 Examiner - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
