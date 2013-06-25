package uk.ac.dotrural.quality.ed.viewer.properties;

public enum Property {
	
	NULL, TEMPERATURE, LIGHT, VIBRATION, MOTION;
	
	public Property getProperty(String propertyStr)
	{
		if(propertyStr.equalsIgnoreCase("temperature"))
			return TEMPERATURE;
		if(propertyStr.equalsIgnoreCase("light"))
			return LIGHT;
		if(propertyStr.equalsIgnoreCase("vibration"))
			return VIBRATION;
		if(propertyStr.equalsIgnoreCase("motion"))
			return MOTION;
		
		return NULL;
	}

}
