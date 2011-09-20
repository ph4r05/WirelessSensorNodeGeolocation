/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.utils;

/**
 *
 * @author ph4r05
 */
/**
 * A class which contains various platform specific utilities.
 */
 
 public class PlatformUtils{
  
  
  
  /**
   * Returns whether the version of Java we're running in is higher than or
   * equal to the specified version.
   */
   
  public static boolean isJavaBetterThan(String ver){
    return System.getProperty("java.version").compareTo(ver) >= 0;
  }
  
  
  
  /**
   * Returns whether we're running under the old (just in case there's ever a
   * new one) Microsoft VM.
   */
   
  public static boolean isOldMicrosoftVM(){
    String vendor = System.getProperty("java.vendor");

    return (vendor != null) && vendor.toLowerCase().startsWith("microsoft") && 
           !isJavaBetterThan("1.2");    
  }
  
  
  
  /**
   * Returns whether we're running under Windows.
   */
   
  public static boolean isWindows(){
    String os = System.getProperty("os.name"); 

    return (os != null) && os.toLowerCase().startsWith("windows");
  }
  
  
  
  /**
   * Returns whether we're running under Windows 95/98/ME.
   */
   
  public static boolean isOldWindows(){
    String os = System.getProperty("os.name");
    
    return isWindows() && (System.getProperty("os.version").compareTo("5.0") < 0) &&
                          !os.toLowerCase().startsWith("windows nt");
  }
  
  
  
  /**
   * Returns whether we're running under Linux.
   */
   
  public static boolean isLinux(){
    String os = System.getProperty("os.name"); 

    return (os != null) && os.toLowerCase().startsWith("linux");
  }
  
  
  
  /**
   * Returns whether we're running under Mac OS.
   */
   
  public static boolean isMacOS(){
    String os = System.getProperty("os.name");

    return (os != null) && os.toLowerCase().startsWith("mac");
  }
  
  
  
  /**
   * Returns whether we're running under Mac OS X.
   */
   
  public static boolean isMacOSX(){
    String os = System.getProperty("os.name");

    return (os != null) && os.toLowerCase().startsWith("mac os x");
  }
  
  
  
  /**
   * Returns whether we're running under Solaris.
   */
   
  public static boolean isSolaris(){
    String os = System.getProperty("os.name");
    
    return (os != null) && (os.toLowerCase().startsWith("solaris") ||
                            os.toLowerCase().startsWith("sunos"));
  }
  
  
  
  /**
   * Returns the name of the OS we're running on, out of the specified list:
   * <ul>
   *   <li>windows
   *   <li>linux
   *   <li>macosx
   *   <li>solaris
   * </ul>
   * 
   * Returns <code>null</code> if we're running on an OS which is not in the
   * above list.
   */
  
  public static String getOSName(){
    if (isWindows() || isOldWindows())
      return "windows";
    else if (isLinux())
      return "linux";
    else if (isMacOSX())
      return "macosx";
    else if (isSolaris())
      return "solaris";
    else
      return null;
  }   
}