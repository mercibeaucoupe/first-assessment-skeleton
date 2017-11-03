package com.cooksys.assessment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("username")
	private String username;
    
    @JsonProperty("command")
	private String command;
    
    @JsonProperty("contents")
	private String contents;
    
    @JsonProperty("time")
    private String time;
    
    @JsonProperty("type")
    private String type;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
	
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

}
