package com.oy.vent.model;

import java.io.Serializable;


public class RowItem  implements Serializable{
	
		private static final long serialVersionUID = 4861597073026532544L;
		
		public int rowId;
		public String photoId;
		public String postDate;		
		public String urllarge;
		public String urlmedium;
		public String urlsmall;
		public String urlthumb;		
		public String username;
		public String ownedby;		   
		public float points;
		public boolean isSection = false;
		public double geo;
	     
	    public RowItem(int rowId,String photoId,String postDate,String urllarge, String urlmedium, String urlsmall, String urlthumb, String username, String ownedby, boolean isSection, float points, double geo){
	    	this.rowId = rowId;
	    	this.photoId = photoId;
	    	this.postDate = postDate;	    	
	    	this.urllarge = urllarge;
	    	this.urlmedium = urlmedium;
	    	this.urlsmall = urlsmall;
	    	this.urlthumb = urlthumb;	    
	    	this.username = username;
	    	this.ownedby = ownedby;
	    	
	    	/*if(eventName != null && eventName != "" && eventName.length() >= 32)
	    	{
	    		this.eventID = eventName.substring(0,32);	    		
	    		this.eventName = eventName.substring(32);	    		
	    	}	*/    	
	    	
	    	this.isSection = isSection;
	    	this.points = points;
	    	this.geo = geo;
	    }
	    
	    @Override
	    public String toString()
	    {
	       return rowId+" "+photoId+"  "+postDate+" "+urlthumb;
	    }
	
	

}
