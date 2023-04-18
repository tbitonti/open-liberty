package com.ibm.ws.cache.servlet;

import java.io.IOException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
             
public class ASynchWebRequest extends Thread {

   String url = null;
   public WebConversation wc = null;
   WebResponse response = null;

   public ASynchWebRequest(String s) {
      url = s;
      wc = new WebConversation();
   }

   public void run() {
      try {
         response = wc.getResponse(new GetMethodWebRequest(url));
      } catch (java.net.MalformedURLException e) {e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}
        catch (org.xml.sax.SAXException e) {e.printStackTrace();}
        
   }

   public WebResponse getWebResponse() {
      while (response == null) {
         try {
            Thread.sleep(100);
         } catch (Exception e) {}
      }
      return response;
   }


}
