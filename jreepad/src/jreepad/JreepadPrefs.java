/*
           Jreepad - personal information manager.
           Copyright (C) 2004 Dan Stowell

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

The full license can be read online here:

           http://www.gnu.org/copyleft/gpl.html
*/

package jreepad;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

/*

A class to hold Jreepad's preferences - and hopefully to store them on disk in a nice 
permanent way which can be carried across from different versions

*/

public class JreepadPrefs implements Serializable
{
  File openLocation, saveLocation, importLocation, exportLocation, backupLocation;
  
  int autoSavePeriod;
  boolean autoSave;
  
  public static final int VIEW_BOTH = 0;
  public static final int VIEW_TREE = 1;
  public static final int VIEW_ARTICLE = 2;
  int viewWhich;
  
  boolean viewToolbar;
  
  int searchMaxNum;

  boolean autoDateInArticles;

  boolean loadLastFileOnOpen;
  
  String webSearchName;
  String webSearchPrefix;
  String webSearchPostfix;
  
  int defaultSearchMode;
  
  boolean wikiBehaviourActive;
  
  static final String[] characterEncodings = 
                 new String[]{"ISO-8859-1", "UTF-8", "UTF-16", "US-ASCII", "8859_15"};
  int fileEncoding;
  String getEncoding()
  {    return characterEncodings[fileEncoding];  }
  
  Vector openRecentList;
  int openRecentListLength;
  
  Font treeFont;
  Font articleFont;
  
  int characterWrapWidth;
  
  boolean wrapToWindow;
  
  JreepadPrefs()
  {
    openLocation = new File(System.getProperty("user.home"));
    
    autoSavePeriod = 10;
    autoSave = false;
    
    viewWhich = 0;
    
    viewToolbar = true;
    
    searchMaxNum = 200;
    
    autoDateInArticles = false;
    
    loadLastFileOnOpen = true;

    webSearchName = "Google search for highlighted text";
    webSearchPrefix = "www.google.co.uk/search?q=";
    webSearchPostfix = "&hl=en";
    
    defaultSearchMode = 0;
    
    wikiBehaviourActive = true;
    
    fileEncoding = 0;
    
    openRecentList = new Vector();
    openRecentListLength = 10;
    
    treeFont = (new JTree()).getFont();
    articleFont = (new JEditorPane()).getFont();
    
    characterWrapWidth = 80;
    
    wrapToWindow = true;
  }
  
  // We override the serialization routines so that different versions of our class can read 
  // each other's serialized states.
  private void writeObject(java.io.ObjectOutputStream out)
     throws IOException
  {
    out.writeObject(openLocation);
    out.writeObject(saveLocation);
    out.writeObject(importLocation);
    out.writeObject(exportLocation);
    out.writeObject(backupLocation);
    
    out.writeInt(autoSavePeriod);
    out.writeBoolean(autoSave);
    
    out.writeInt(viewWhich);
    
    out.writeBoolean(viewToolbar);
    
    out.writeInt(searchMaxNum);
    
    out.writeBoolean(autoDateInArticles);
    
    out.writeBoolean(loadLastFileOnOpen);

    out.writeObject(webSearchName);
    out.writeObject(webSearchPrefix);
    out.writeObject(webSearchPostfix);

    out.writeInt(defaultSearchMode);
    
    out.writeBoolean(wikiBehaviourActive);

    out.writeInt(fileEncoding);

    out.writeObject(openRecentList);
    out.writeInt(openRecentListLength);

    out.writeObject(treeFont);
    out.writeObject(articleFont);
    
    out.writeInt(characterWrapWidth);
    
    out.writeBoolean(wrapToWindow);
  }
  private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
  {
   try
   {
    openLocation = (File)in.readObject();
    saveLocation = (File)in.readObject();
    importLocation = (File)in.readObject();
    exportLocation = (File)in.readObject();
    backupLocation = (File)in.readObject();
    
    autoSavePeriod = in.readInt();
    autoSave = in.readBoolean();
    
    viewWhich = in.readInt();
    
    viewToolbar = in.readBoolean();
    
    searchMaxNum = in.readInt();
    
    autoDateInArticles = in.readBoolean();
    
    loadLastFileOnOpen = in.readBoolean();

    webSearchName = (String)in.readObject();
    webSearchPrefix = (String)in.readObject();
    webSearchPostfix = (String)in.readObject();

    defaultSearchMode = in.readInt();
    
    wikiBehaviourActive = in.readBoolean();
 
    fileEncoding = in.readInt();
    
    openRecentList = (Vector)in.readObject();
    openRecentListLength = in.readInt();

    treeFont = (Font)in.readObject();
    articleFont = (Font)in.readObject();
    
    characterWrapWidth = in.readInt();
    
    wrapToWindow = in.readBoolean();
   }
   catch(EOFException e)
   {
   }
 } 
}