package com.pos.passport.model;

import com.pos.passport.interfaces.ItemOpen;

import java.io.Serializable;

public class SectionItem implements ItemOpen,Serializable {

	private final String title;
	
	public SectionItem(String title) {
		this.title = title;
	}
	
	public String getTitle(){
		return title;
	}
	
	@Override
	public boolean isSection() {
		return true;
	}

}
