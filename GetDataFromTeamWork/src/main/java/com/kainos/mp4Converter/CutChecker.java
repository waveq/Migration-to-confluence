package com.kainos.mp4Converter;

import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;

public class CutChecker extends MediaToolAdapter {
	/** {@inheritDoc} */

	public Long timeInMilisec = (long) 0;

	@Override
	public void onVideoPicture(IVideoPictureEvent event) {
		timeInMilisec = event.getTimeStamp();
		super.onVideoPicture(event);
	}
}