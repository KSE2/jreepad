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

import java.util.*;
import java.io.*;
import javax.swing.tree.*;

public class JreepadNode implements Serializable, TreeNode, MutableTreeNode, Comparable
{
  private Vector children;
  private String title;
  private String content;
//  private int childrenCount=0;
  private JreepadNode parentNode;
  private OurSortComparator ourSortComparator;

  public JreepadNode()
  {
    this((JreepadNode)null);
  }
  public JreepadNode(JreepadNode parentNode)
  {
    this("", parentNode);
  }
  public JreepadNode(String content, JreepadNode parentNode)
  {
    this("<Untitled node>",content, parentNode);
  }
  public JreepadNode(String title, String content, JreepadNode parentNode)
  {
    this.title = title;
    this.content = content;
    this.parentNode = parentNode;
    ourSortComparator = new OurSortComparator();
    children = new Vector();
  }
  public JreepadNode(InputStreamReader treeInputStream) throws IOException
  {
    constructFromInputStream(treeInputStream);
  }
  private void constructFromInputStream(InputStreamReader treeInputStream) throws IOException
  {
    int lineNum = 2;
    int depthMarker;
    BufferedReader bReader = new BufferedReader(treeInputStream);
    JreepadNode babyNode;
    children = new Vector();

    Stack nodeStack = new Stack();
    nodeStack.push(this);
    
    int hjtFileFormat = -1;

    String dtLine, nodeLine, titleLine, depthLine;
    StringBuffer currentContent;
    String currentLine = bReader.readLine(); // Read the first line, check for treepadness
    if((currentLine.toLowerCase().startsWith("<treepad") && currentLine.endsWith(">")) )
    {
      hjtFileFormat = 1;
    }
    else if((currentLine.toLowerCase().startsWith("<hj-treepad") && currentLine.endsWith(">")) )
    {
      hjtFileFormat = 2;
    }
    else
    {
      throw new IOException("\"<Treepad>\" tag not found at beginning of file!\n(This can be caused by having the wrong character set specified.)");
    }
    
    dtLine = "dt=text";

    while(       (hjtFileFormat == 2 ||  (dtLine = bReader.readLine())!=null)
         && (nodeLine = bReader.readLine())!=null && 
          (titleLine = bReader.readLine())!=null && (depthLine = bReader.readLine())!=null)
    {
      // Read "dt=text"    [or error] - NB THE OLDER FORMAT DOESN'T INCLUDE THIS LINE SO WE SKIP IT
      if(dtLine.equals("") && nodeLine.startsWith("<bmarks>"))
        throw new IOException("This is not a Treepad-Lite-compatible file!\n\nFiles created in more advanced versions of Treepad\ncontain features that are not available in Jreepad.");

      if(hjtFileFormat != 2)
        if(! (dtLine.toLowerCase().startsWith("dt=text")))
          throw new IOException("Unrecognised node dt format at line " + lineNum + ": " + dtLine);
      // Read "<node>"     [or error]
      if(! (nodeLine.toLowerCase().startsWith("<node>")))
        throw new IOException("Unrecognised node format at line " + (lineNum+1) + ": " + nodeLine);

//      System.out.println("Found node (depth " + depthLine + "): " + titleLine);

      lineNum += 4;

      // Read THE CONTENT! [loop until we find "<end node> 5P9i0s8y19Z"]
      currentContent = new StringBuffer();
      while((currentLine = bReader.readLine())!=null && !currentLine.equals("<end node> 5P9i0s8y19Z"))
      {
        currentContent.append(currentLine + "\r\n");
        lineNum++;
      }

      // Now, having established the content and the title and the depth, we'll create the child
      babyNode = new JreepadNode(titleLine, currentContent.substring(0, Math.max(currentContent.length()-2,0)),
                             (JreepadNode)(nodeStack.peek()));
//      babyNode = new JreepadNode(titleLine, currentContent.toString());

      if(depthLine.equals("0"))
      {
        this.title = titleLine;
        this.content = currentContent.toString();
      }
      else
      {
        depthMarker = Integer.parseInt(depthLine);
        while(nodeStack.size()>depthMarker)
          nodeStack.pop();

 //       System.out.println("   Adding it to a ");

        ((JreepadNode)(nodeStack.peek())).addChild(babyNode);
        nodeStack.push(babyNode);

        // Remember THE LEVEL!   [single integer: 0 is root node, 1 is children of root, etc.]
      
        // Now add the correct node to the right point in the tree
      }
    }

  } // End of constructor from FileInputStream

  public String toString()
  {
    return getTitle();
  }
  public String toFullString()
  {
    String ret = "JreepadNode \""+getTitle()+"\": " + getChildCount() + " direct child nodes in subtree";
    ret += "\r\nDirect children:";
    for(int i=0; i<children.size(); i++)
      ret += "\r\n    " + ((JreepadNode)getChildAt(i)).getTitle();
    return ret;
  }

  public String exportAsHtml()
  {
    return exportAsHtml(true).toString();
  }
  public StringBuffer exportAsHtml(boolean isRoot)
  {
    StringBuffer ret = new StringBuffer();
    if(isRoot)
    {
      ret.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">\n<head>\n<title>");
      ret.append(htmlSpecialChars(getTitle()));
      ret.append("</title>\n<style type=\"text/css\">\n"
        + "dl {}\ndl dt { font-weight: bold; margin-top: 10px; font-size: 24pt; }\ndl dd {margin-left: 20px; padding-left: 0px;}\ndl dd dl dt {background: black; color: white; font-size: 12pt; }\ndl dd dl dd dl dt {background: white; color: black; }"
        + "\n</style>\n</head>\n\n<body>\n<!-- Exported from Jreepad -->\n<dl>");
    }
    ret.append("\n<dt>");
    ret.append(htmlSpecialChars(getTitle()));
    ret.append("</dt>\n<dd>");
    ret.append(htmlSpecialChars(getContent()));
    if(children.size()>0)
      ret.append("\n<dl>");
    for(int i=0; i<children.size(); i++)
      ret.append(((JreepadNode)getChildAt(i)).exportAsHtml(false));
    if(children.size()>0)
      ret.append("\n</dl>");
    ret.append("</dd>");

    if(isRoot)
      ret.append("\n</dl>\n</body>\n</html>");
    return ret;
  }
  private String htmlSpecialChars(String in)
  {
    char[] c = in.toCharArray();
    StringBuffer ret = new StringBuffer();
    for(int i=0; i<c.length; i++)
      if(c[i]=='<')
        ret.append("&lt;");
      else if(c[i]=='>')
        ret.append("&gt;");
      else if(c[i]=='&')
        ret.append("&amp;");
      else if(c[i]=='\n')
        ret.append("<br />\n");
      else
        ret.append(c[i]);
    return ret.toString();
  }

  public String exportAsSimpleXml()
  {
    return exportAsSimpleXml(true).toString();
  }
  public StringBuffer exportAsSimpleXml(boolean isRoot)
  {
    StringBuffer ret = new StringBuffer();
    if(isRoot)
    {
      ret.append("<?xml version=\"1.0\" standalone=\"yes\" encoding=\"UTF-8\"?>");
    }
    ret.append("\n<node title=\">");
    ret.append(getTitle());
    ret.append("\">");
    ret.append(getContent());
    for(int i=0; i<children.size(); i++)
      ret.append(((JreepadNode)getChildAt(i)).exportAsSimpleXml(false));
    ret.append("</node>");
    return ret;
  }

  public String exportTitlesAsList()
  {
    return exportTitlesAsList(0).toString();
  }
  public StringBuffer exportTitlesAsList(int currentDepth)
  {
    StringBuffer ret = new StringBuffer();
    for(int i=0; i<currentDepth; i++)
      ret.append(" ");
    ret.append(getTitle() + "\r\n");
    for(int i=0; i<children.size(); i++)
      ret.append(((JreepadNode)getChildAt(i)).exportTitlesAsList(currentDepth+1));

    return ret;
  }


  private void writeObject(ObjectOutputStream out) throws IOException
  {
    out.writeBytes(this.toTreepadString());
  }
//  private void writeObject(OutputStreamWriter out) throws IOException
//  {
//    out.write(this.toTreepadString());
//  }
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    constructFromInputStream(new InputStreamReader(in));
  }
  public String toTreepadString()
  {
    return "<Treepad version 2.7>\r\n" + toTreepadString(0);
  }
  public String toTreepadString(int currentDepth)
  {
    StringBuffer ret = new StringBuffer("dt=Text\r\n<node>\r\n");
    ret.append(getTitle() + "\r\n" + (currentDepth++) + "\r\n" + getContent() + "\r\n<end node> 5P9i0s8y19Z\r\n");
    for(int i=0; i<getChildCount(); i++)
      ret.append(((JreepadNode)getChildAt(i)).toTreepadString(currentDepth));
    return ret.toString();
  }

  public void addChild(JreepadNode child)
  {
    children.add(child);
    child.setParent(this);
  }
  public JreepadNode removeChild(int child) // Can be used to delete, OR to 'get' one for moving
  {
    if(child<0 || child > children.size()) return null;
    
    JreepadNode ret = (JreepadNode)children.remove(child);
    return ret;
  }
  public TreeNode getChildAt(int child)
  {
    if(child<0 || child>= children.size())
      return null;
    else
      return (JreepadNode)children.get(child);
  }
  public int getChildCount()
  {
    return children.size();
  }
  public boolean indent()
  {
    // Get position in parent. If zero or -1 then return.
    int pos = getIndex();
    if(pos<1) return false;
    // Get sibling node just above, and move self to there.
    getParentNode().removeChild(pos);
    JreepadNode newParent = (JreepadNode)getParentNode().getChildAt(pos-1);
    newParent.addChild(this);
    setParent(newParent);
    return true;
  }
  public boolean outdent()
  {
    // Get parent's parent. If null then return.
    JreepadNode p = (JreepadNode)getParent();
    if(p==null) return false;
    JreepadNode pp = (JreepadNode)p.getParent();
    if(pp==null) return false;
    // Get parent's position in its parent. = ppos
    int ppos = p.getIndex();
    // Move self to parent's parent, at (ppos+1)
    p.removeChild(getIndex());
    pp.insert(this, ppos+1);
    setParent(pp);

    // Also (as in the original treepad) move all the later siblings so they're children of this node


    // NOT DONE YET


    return true;
  }
  public void moveChildUp(int child)
  {
    if(child<1 || child>= children.size())
      return;

    children.add(child-1, children.remove(child));
  }
  public void moveChildDown(int child)
  {
    if(child<0 || child>= children.size()-1)
      return;

    children.add(child+1, children.remove(child));
  }
  public void moveUp()
  {
    if(getParentNode()==null) return;
    int index = getIndex();
    if(index<1) return;
    
    removeFromParent();
    getParentNode().insert(this, index-1);
  }
  public void moveDown()
  {
    if(getParentNode()==null) return;
    int index = getIndex();
    if(index<0 || index >= getParentNode().getChildCount()-1) return;
    
    removeFromParent();
    getParentNode().insert(this, index+1);
  }
  public JreepadNode addChild()
  {
    JreepadNode theChild = new JreepadNode(this);
    children.add(theChild);
    theChild.setParent(this);
    return theChild;
  }
  public JreepadNode addChild(int index)
  {
    JreepadNode theChild = new JreepadNode(this);
    children.add(index, theChild);
    theChild.setParent(this);
    return theChild;
  }
  
  public int getIndex(TreeNode child)
  {
    for(int i=0; i<getChildCount(); i++)
      if(((JreepadNode)child).equals(getChildAt(i)))
        return i;
    return -1;
  }
  public int getIndex()
  {
    if(getParent()==null)
      return -1;
    return getParent().getIndex(this);
  }
  
  public boolean isNodeInSubtree(JreepadNode n)
  {
    for(int i=0; i<getChildCount(); i++)
    {
      JreepadNode aChild = (JreepadNode)getChildAt(i);
      if(aChild.equals(n) || aChild.isNodeInSubtree(n))
        return true;
    }
    return false;
  }
  
  public void sortChildren()
  {
    sort();
  }

  public void sortChildrenRecursive()
  {
    sort();
    for(int i=0; i<getChildCount(); i++)
      ((JreepadNode)getChildAt(i)).sortChildrenRecursive();
  }

  // Function for using Java's built-in mergesort
  private void sort()
  {
    Object[] childrenArray = children.toArray();
    java.util.Arrays.sort(childrenArray, ourSortComparator);
    children = new Vector();
    for(int i=0; i<childrenArray.length; i++)
      children.add((JreepadNode)childrenArray[i]);
  }
  private class OurSortComparator implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      return ((JreepadNode)o1).getTitle().compareToIgnoreCase(
            ((JreepadNode)o2).getTitle());
    }
    public boolean equals(Object obj)
    {
      return obj.equals(this); // Lazy!
    }
  }
  public int compareTo(Object o)
  {
    return getTitle().compareToIgnoreCase(
            ((JreepadNode)o).getTitle());
  }
  // End of: Stuff to use Java's built-in mergesort

  public boolean getAllowsChildren() { return true; } // Required by TreeNode interface
  public boolean isLeaf()
  {
    return getChildCount()==0; // Is this the correct behaviour?
  }
  
  public Enumeration children()
  {
    return new JreepadNodeEnumeration();
  } // Required by TreeNode interface

  public JreepadNode getParentNode()
  {
    return parentNode;
  }
  public TreeNode getParent()
  {
    return parentNode;
  }

  // MutableTreeNode functions
  public void remove(int child)
  {
    removeChild(child);
  }
  public void remove(MutableTreeNode node)
  {
    removeChild(getIndex((JreepadNode)node));
  }
  public void removeFromParent()
  {
    if(parentNode != null)
      parentNode.remove(this);
  }
  public void setParent(MutableTreeNode parent)
  {
    parentNode = (JreepadNode)parent; // Do we need to do anything more at this point?
  }

  public void setUserObject(Object object)
  {
    // ?
    setContent(object.toString());
  }
  public void insert(MutableTreeNode child, int index)
  {
    children.insertElementAt((JreepadNode)child, index);
  }

  public void addChildFromTextFile(File textFile) throws IOException
  {
    // Load the content as a string
    StringBuffer contentString = new StringBuffer();
    String currentLine;
    BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
    while((currentLine = bReader.readLine())!=null)
      contentString.append(currentLine + "\n");
    // Then just create the node
    addChild(new JreepadNode(textFile.getName(), contentString.toString(), this));
  }

  // This getCopy() function is intended to return a copy of the entire subtree, used for Undo
  public JreepadNode getCopy()
  {
    JreepadNode ret = new JreepadNode(getTitle(), getContent(), null);
    for(int i=0; i<getChildCount(); i++)
    {
      ret.addChild(((JreepadNode)getChildAt(i)).getCopy());
    }
    return ret;
  }
  
  public JreepadNode getChildByTitle(String title)
  {
    for(int i=0; i<getChildCount(); i++)
      if(((JreepadNode)getChildAt(i)).getTitle().equals(title))
        return (JreepadNode)getChildAt(i);
    return null;
  }

  public String getTitle() { return title; }
  public String getContent() { return content; }
  public void setTitle(String title) { this.title = title; }
  public void setContent(String content) { this.content = content; }


  public class JreepadNodeEnumeration implements Enumeration
  {
    private int i=0;
    public boolean hasMoreElements() { return i<getChildCount(); }
    public Object nextElement()      { return getChildAt(i++);   }
  } // This enumerator class is required by the TreeNode interface
}
