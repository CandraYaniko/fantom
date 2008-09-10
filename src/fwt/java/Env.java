//
// Copyright (c) 2008, Brian Frank and Andy Frank
// Licensed under the Academic Free License version 3.0
//
// History:
//   12 Jun 08  Brian Frank  Creation
//
package fan.fwt;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import fan.sys.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Display;

/**
 * Env manages the display resources.
 */
public class Env
{

//////////////////////////////////////////////////////////////////////////
// Lookup
//////////////////////////////////////////////////////////////////////////

  /**
   * Get the Env for the current thread.
   */
  public static Env get()
  {
    return (Env)threadlocal.get();
  }

  private static ThreadLocal threadlocal = new ThreadLocal()
  {
    protected Object initialValue() { return new Env(); }
  };

  private Env() {}

//////////////////////////////////////////////////////////////////////////
// Display
//////////////////////////////////////////////////////////////////////////

  public void mainEventLoop(Shell shell)
  {
    eventLoop(shell);
    display.dispose();
    disposeAllColors();
    disposeAllFonts();
    disposeAllImages();
    disposeScratchGC();
  }

  public void eventLoop(Shell shell)
  {
    while (!shell.isDisposed())
    {
      try
      {
        if (!display.readAndDispatch())
          display.sleep();
      }
      catch (Throwable e)
      {
        e.printStackTrace();
      }
    }
  }

//////////////////////////////////////////////////////////////////////////
// Color
//////////////////////////////////////////////////////////////////////////

  /**
   * Map a Fan Color to an SWT color.
   */
  public Color color(fan.fwt.Color c)
  {
    if (c == null) return null;
    Color x = (Color)colors.get(c.argb);
    if (x == null)
    {
      int argb = (int)c.argb.val;
      x = new Color(display, (argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff);
      colors.put(c.argb, x);
    }
    return x;
  }

  /**
   * Dispose the SWT color for the Fan Color.
   */
  public void dispose(fan.fwt.Color c)
  {
    if (c == null) return;
    Color x = (Color)colors.get(c.argb);
    if (x != null)
    {
      x.dispose();
      colors.remove(c.argb);
    }
  }

  /**
   * Dispose all cached colors.
   */
  public void disposeAllColors()
  {
    Iterator it = (Iterator)colors.values().iterator();
    while (it.hasNext()) ((Color)it.next()).dispose();
    colors.clear();
  }

//////////////////////////////////////////////////////////////////////////
// Font
//////////////////////////////////////////////////////////////////////////

  /**
   * Map a Fan Font to an SWT Font.
   */
  public Font font(fan.fwt.Font f)
  {
    if (f == null) return null;
    Font x = (Font)fonts.get(f);
    if (x == null)
    {
      int style = SWT.NORMAL;
      if (f.bold.val) style |= SWT.BOLD;
      if (f.italic.val) style |= SWT.ITALIC;
      x = new Font(display, f.name.val, (int)f.size.val, style);
      fonts.put(f, x);
    }
    return x;
  }

  /**
   * Dispose the SWT font for the Fan Font.
   */
  public void dispose(fan.fwt.Font f)
  {
    if (f == null) return;
    Font x = (Font)fonts.get(f);
    if (x != null)
    {
      x.dispose();
      fonts.remove(f);
    }
  }

  /**
   * Dispose all cached fonts.
   */
  public void disposeAllFonts()
  {
    Iterator it = (Iterator)fonts.values().iterator();
    while (it.hasNext()) ((Font)it.next()).dispose();
    fonts.clear();
  }

//////////////////////////////////////////////////////////////////////////
// Images
//////////////////////////////////////////////////////////////////////////

  /**
   * Map a Fan Image to an SWT Image.
   */
  public Image image(fan.fwt.Image i)
  {
    if (i == null) return null;
    FileImage fi = (FileImage)i;
    Image x = (Image)images.get(fi.file.uri());
    if (x == null)
    {
      InputStream in = SysInStream.java(fi.file.in());
      try
      {
        x = new Image(display, in);
        images.put(fi.file.uri(), x);
      }
      catch (Exception e)
      {
        System.out.println("ERROR: Cannot load image: " + i);
        e.printStackTrace();
        return null;
      }
      finally
      {
        try { in.close(); } catch (Exception e) {}
      }
    }
    return x;
  }

  /**
   * Dispose the SWT image for the Fan Image.
   */
  public void dispose(fan.fwt.Image i)
  {
    if (i == null) return;
    FileImage fi = (FileImage)i;
    Image x = (Image)images.get(fi.file.uri());
    if (x != null)
    {
      x.dispose();
      images.remove(fi.file.uri());
    }
  }

  /**
   * Dispose all cached images.
   */
  public void disposeAllImages()
  {
    Iterator it = (Iterator)images.values().iterator();
    while (it.hasNext()) ((Image)it.next()).dispose();
    images.clear();
  }

//////////////////////////////////////////////////////////////////////////
// Scratch GC
//////////////////////////////////////////////////////////////////////////

  /**
   * SWT makes it extremely paintful to work with font metrics,
   * so we use a scratch GC to do font metrics conveniently.
   */
  public GC scratchGC()
  {
    if (scratchGC == null) scratchGC = new GC(display);
    return scratchGC;
  }

  /**
   * Dispose the scratchGC if we've allocated one.
   */
  void disposeScratchGC()
  {
    if (scratchGC != null)
    {
      scratchGC.dispose();
      scratchGC = null;
    }
  }

//////////////////////////////////////////////////////////////////////////
// Platform
//////////////////////////////////////////////////////////////////////////

  public static boolean isWindows() { return SWT.getPlatform().equals("win32"); }
  public static boolean isMac() { return SWT.getPlatform().equals("carbon"); }

  public static int os(String name)
  {
    try
    {
      Class c = Class.forName("org.eclipse.swt.internal." + SWT.getPlatform() + ".OS");
      return c.getField(name).getInt(null);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0;
    }
  }

  public static int osGet(Control w, int k)
  {
    try
    {
      Class c = Class.forName("org.eclipse.swt.internal." + SWT.getPlatform() + ".OS");
      Method m = c.getMethod("GetWindowLong", new Class[] { int.class, int.class });
      return ((Integer)m.invoke(null, new Object[] {w.handle, new Integer(k)})).intValue();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 0;
    }
  }

  public static void osSet(Control w, int k, int v)
  {
    try
    {
      Class c = Class.forName("org.eclipse.swt.internal." + SWT.getPlatform() + ".OS");
      Method m = c.getMethod("SetWindowLong", new Class[] { int.class, int.class, int.class });
      m.invoke(null, new Object[] {w.handle, new Integer(k), new Integer(v)});
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

  Display display = new Display(); // SWT display
  fan.fwt.Display fanDisplay;      // Fan fwt::Display
  HashMap colors = new HashMap();  // Int rgb   -> Color
  HashMap fonts = new HashMap();   // fwt::Font  -> Font
  HashMap images = new HashMap();  // Uri -> Image
  GC scratchGC;

}
