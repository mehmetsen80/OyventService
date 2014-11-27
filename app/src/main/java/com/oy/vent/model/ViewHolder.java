package com.oy.vent.model;

import com.oy.vent.R;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder 
{
	private View mRow;
	private TextView photoId = null;
	private TextView postdate = null;		
	private ImageView thumb =null; 
	private TextView fullname = null;
	private TextView username = null;
	private TextView eventName = null;	
	private TextView seperatorHeader = null;	
	private ImageButton userthumb2 = null;
	private ImageButton feedDelete = null;
	private TextView points = null;
	private TextView geo = null;
	
	public ViewHolder(View row)
	{
      mRow = row;
	}
	
	public TextView getPhotoId()
	{
		if(null == photoId)
		{
			photoId = (TextView) mRow.findViewById(R.id.feed_id);
		}
		return photoId;
	}
	  
	public TextView getPostDate()
	{
		if(null == postdate)
		{
			postdate = (TextView) mRow.findViewById(R.id.feed_postdate);
		}
		return postdate;
	}
	public ImageView getThumb()
	{
      if(null == thumb)
      { 
    	  thumb = (ImageView) mRow.findViewById(R.id.feed_thumb);
      }
     return thumb;
	}

	/*public TextView getSeperatorHeader() {				
		
		if(null == seperatorHeader)
		{
			seperatorHeader = (TextView) mRow.findViewById(R.id.seperator_header);
		}
		return seperatorHeader;				
	}*/
	
	
	
	public TextView getUserName() {				
		
		if(null == username)
		{
			username = (TextView) mRow.findViewById(R.id.feed_username);
		}
		return username;				
	}
	
	/*public TextView getEventName() {				
		
		if(null == eventName)
		{
			eventName = (TextView) mRow.findViewById(R.id.tag_Name1);
		}
		return eventName;				
	}*/
	
	
	/*public ImageButton getFeedDelete(){
		if(null == feedDelete)
		{
			feedDelete = (ImageButton) mRow.findViewById(R.id.feed_delete);
		}
		
		return feedDelete;
	}*/
	
	public TextView getPoints() {				
		
		if(null == points)
		{
			points = (TextView) mRow.findViewById(R.id.feed_points);
		}
		return points;				
	}
	
	public TextView getGeo() {				
		
		if(null == geo)
		{
			geo = (TextView) mRow.findViewById(R.id.feed_geo);
		}
		return geo;				
	}
	
	
	
	
	/*public MediaController getMediaController()
	{
		if(null == mediaController)
		{
			mediaController = (MediaController) mRow.findViewById(R.id.mediaController);
		}
		
		return mediaController;
	}*/
	  
}//end of ViewHolder
