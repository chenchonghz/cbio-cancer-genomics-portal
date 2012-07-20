package org.mskcc.cbio.cgds.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Useful for unit testing code that uses HttpServletRequest.
 * From http://www.jguru.com/faq/view.jsp?EID=110660
 */
public class NullHttpServletRequest implements HttpServletRequest{
   private Hashtable parameters = new Hashtable();
      
   public void setParameter(String key, String value) {
      parameters.put(key, value);
   }
   
   public String getParameter(String key) {
      return (String)this.parameters.get(key);
   }

   public Enumeration getParameterNames(){
      return this.parameters.keys();
  }

   public Cookie[] getCookies() {return null;}
   public String getMethod(){return null;}
   public String getRequestURI(){return null;}
   public String getServletPath(){return null;}
   public String getPathInfo(){return null;}
   public String getPathTranslated(){return null;}
   public String getQueryString(){return null;}
   public String getRemoteUser(){return null;}
   public String getAuthType(){return null;}
   public String getHeader(String name){return null;}
   public int getIntHeader(String name){return 0;}
   public long getDateHeader(String name){return 0;}
   public Enumeration getHeaderNames(){return null;}
   public HttpSession getSession(boolean create){return null;}
   public String getRequestedSessionId(){return null;}
   public boolean isRequestedSessionIdValid(){return false;}
   public boolean isRequestedSessionIdFromCookie(){return false;}
   public boolean isRequestedSessionIdFromUrl(){return false;}
   public int getContentLength(){return 0;}
   public String getContentType(){return null;}
   public String getProtocol(){return null;}
   public String getScheme(){return null;}
   public String getServerName(){return null;}
   public int getServerPort(){return 0;}
   public String getRemoteAddr(){return null;}
   public String getRemoteHost(){return null;}
   public String getRealPath(String path){return null;}
   public ServletInputStream getInputStream() throws IOException{return null;}
   public String[] getParameterValues(String name){return null;}
   public Enumeration getAttributeNames(){return null;}
   public Object getAttribute(String name){return null;}
   public HttpSession getSession(){return null;}
   public BufferedReader getReader() throws IOException{return null;}
   public String getCharacterEncoding(){return null;}
   public void setAttribute(String name, Object o) {}
   public boolean isRequestedSessionIdFromURL() {return false;}

   public Locale getLocale() {
      // TODO Auto-generated method stub
      return null;
   }

   public Enumeration getLocales() {
      // TODO Auto-generated method stub
      return null;
   }

   public Map getParameterMap() {
      // TODO Auto-generated method stub
      return null;
   }

   public RequestDispatcher getRequestDispatcher(String arg0) {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean isSecure() {
      // TODO Auto-generated method stub
      return false;
   }

   public void removeAttribute(String arg0) {
      // TODO Auto-generated method stub
      
   }

   public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
      // TODO Auto-generated method stub
      
   }

   public String getContextPath() {
      // TODO Auto-generated method stub
      return null;
   }

   public Enumeration getHeaders(String arg0) {
      // TODO Auto-generated method stub
      return null;
   }

   public StringBuffer getRequestURL() {
      // TODO Auto-generated method stub
      return null;
   }

   public Principal getUserPrincipal() {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean isUserInRole(String arg0) {
      // TODO Auto-generated method stub
      return false;
   }
}