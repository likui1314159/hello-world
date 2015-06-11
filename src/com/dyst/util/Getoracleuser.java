package com.dyst.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Getoracleuser {

	public static Properties props = new Properties(); 
	public static  Properties getProperties()
	{
      InputStream in = Getoracleuser.class.getResourceAsStream("/oracleuser.properties");;    
      try{   
          props.load(in);   
      } catch(IOException e){   
          e.printStackTrace();   
      } 
      return props;
	}
}
