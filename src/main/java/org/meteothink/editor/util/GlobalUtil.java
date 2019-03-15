 /* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteothink.editor.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Template
 *
 * @author Yaqiang Wang
 */
public class GlobalUtil {
    // <editor-fold desc="Variables">
    // </editor-fold>
    // <editor-fold desc="Constructor">
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">
    // </editor-fold>
    // <editor-fold desc="Methods">
    
    /**
     * Get software version
     * @return Software version
     */
    public static String getVersion(){
        return "1.7.6";
    }

    /**
     * Get file extension
     *
     * @param filePath The file path
     * @return File extension
     */
    public static String getFileExtension(String filePath) {
        String extension = "";
        String fn = new File(filePath).getName();
        if (fn.contains(".")) {
            String ext = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase().trim();
            try {
                extension = ext;
            } catch (IllegalArgumentException e) {
            }
        }

        return extension;
    }

    /**
     * Get the list of specific file types in a directory
     *
     * @param directory The directory
     * @param ext the file extension
     * @return File name list
     */
    public static List<String> getFiles(String directory, String ext) {
        List<String> fileNames = new ArrayList<>();
        try {
            File f = new File(directory);
            boolean flag = f.isDirectory();
            if (flag) {
                File fs[] = f.listFiles();
                for (int i = 0; i < fs.length; i++) {
                    if (!fs[i].isDirectory()) {
                        String filename = fs[i].getAbsolutePath();
                        if (filename.endsWith(ext.trim())) {
                            fileNames.add(filename);
                        }
                    } else {
                        fileNames.addAll(getFiles(fs[i].getAbsolutePath(), ext));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileNames;
    }

    /**
     * Get sub directories
     *
     * @param directory The directory
     * @return Sub directories
     */
    public static List<String> getSubDirectories(String directory) {
        List<String> subDirs = new ArrayList<>();
        File f = new File(directory);
        File fs[] = f.listFiles();
        for (File f1 : fs) {
            if (f1.isDirectory()) {
                subDirs.add(f1.getPath());
            }
        }

        return subDirs;
    }

    /**
     * Get class names in a jar file
     *
     * @param jarFileName The jar file name
     * @return The class names in the jar file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> getClassNames(String jarFileName) throws FileNotFoundException, IOException {
        List<String> classNames = new ArrayList<>();
        ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFileName));
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
                // This ZipEntry represents a class. Now, what class does it represent?
                StringBuilder className = new StringBuilder();
                for (String part : entry.getName().split("/")) {
                    if (className.length() != 0) {
                        className.append(".");
                    }
                    className.append(part);
                    if (part.endsWith(".class")) {
                        className.setLength(className.length() - ".class".length());
                    }
                }
                classNames.add(className.toString());
            }
        }

        return classNames;
    }

    /**
     * Get the class name which implements IPlugin interface
     *
     * @param jarFileName The jar file name
     * @return The class name which implements IPlugin interface
     */
    public static String getPluginClassName(String jarFileName) {
        String pluginClassName = null;
        try {
            List<String> classNames = getClassNames(jarFileName);
            URL url = new URL("file:" + jarFileName);
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{url});
            for (String name : classNames) {
                Class<?> clazz = urlClassLoader.loadClass(name);
                if (isInterface(clazz, "org.meteoinfo.plugin.IPlugin")) {
                    pluginClassName = name;
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GlobalUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GlobalUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GlobalUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return pluginClassName;
    }

    /**
     * Given a package name, attempts to reflect to find all classes within the
     * package on the local file system.
     *
     * @param packageName
     * @return Class list
     */
    public static List<Class> getClassesInPackage(String packageName) {
        List<Class> classes = new ArrayList<>();
        String packageNameSlashed = "/" + packageName.replace(".", "/");
        // Get a File object for the package  
        URL directoryURL = Thread.currentThread().getContextClassLoader().getResource(packageNameSlashed);
        if (directoryURL == null) {
            System.out.println("Could not retrieve URL resource: " + packageNameSlashed);
            return classes;
        }

        String directoryString = directoryURL.getFile();
        if (directoryString == null) {
            System.out.println("Could not find directory for URL resource: " + packageNameSlashed);
            return classes;
        }

        File directory = new File(directoryString);
        if (directory.exists()) {
            // Get the list of the files contained in the package  
            String[] files = directory.list();
            for (String fileName : files) {
                // We are only interested in .class files  
                if (fileName.endsWith(".class")) {
                    // Remove the .class extension  
                    fileName = fileName.substring(0, fileName.length() - 6);
                    try {
                        classes.add(Class.forName(packageName + "." + fileName));
                    } catch (ClassNotFoundException e) {
                        System.out.println(packageName + "." + fileName + " does not appear to be a valid class.");
                    }
                }
            }
        } else {
            System.out.println(packageName + " does not appear to exist as a valid package on the file system.");
        }
        return classes;
    }
    
    /**
     * Given a package name, attempts to reflect to find all file names within the
     * package on the local file system.
     *
     * @param packageName
     * @return File names
     */
    public static List<String> getFilesInPackage(String packageName) {
        List<String> fns = new ArrayList<>();
        //String packageNameSlashed = "/" + packageName.replace(".", "/");
        String packageNameSlashed = packageName.replace(".", "/");
        // Get a File object for the package  
        URL directoryURL = Thread.currentThread().getContextClassLoader().getResource(packageNameSlashed);
        if (directoryURL == null) {
            System.out.println("Could not retrieve URL resource: " + packageNameSlashed);
            return fns;
        }

        String directoryString = directoryURL.getFile();
        if (directoryString == null) {
            System.out.println("Could not find directory for URL resource: " + packageNameSlashed);
            return fns;
        }

        File directory = new File(directoryString);
        if (directory.exists()) {
            // Get the list of the files contained in the package  
            String[] files = directory.list();
            for (String fileName : files) {
                fns.add(fileName);
            }
        } else {
            System.out.println(packageName + " does not appear to exist as a valid package on the file system.");
        }
        return fns;
    }

    /**
     * Determine if a class implements a interface
     *
     * @param c The class
     * @param szInterface The interface name
     * @return Boolean
     */
    public static boolean isInterface(Class c, String szInterface) {
        Class[] face = c.getInterfaces();
        for (int i = 0, j = face.length; i < j; i++) {
            if (face[i].getName().equals(szInterface)) {
                return true;
            } else {
                Class[] face1 = face[i].getInterfaces();
                for (int x = 0; x < face1.length; x++) {
                    if (face1[x].getName().equals(szInterface)) {
                        return true;
                    } else if (isInterface(face1[x], szInterface)) {
                        return true;
                    }
                }
            }
        }
        if (null != c.getSuperclass()) {
            return isInterface(c.getSuperclass(), szInterface);
        }
        return false;
    }

    /**
     * Get root path
     *
     * @param aFile The file
     * @return Root path
     */
    public static String getPathRoot(File aFile) {
        File path = aFile.getParentFile();
        String pathRoot = path.toString();
        while (path != null) {
            path = path.getParentFile();
            if (path != null) {
                pathRoot = path.toString();
            }
        }

        return pathRoot;
    }

    /**
     * Get relative path of the file using project file path
     *
     * @param fileName File path
     * @param projFile Project file path
     * @return Relative path
     * @throws java.io.IOException
     */
    public static String getRelativePath(String fileName, String projFile) throws IOException {
        String RelativePath = "";
        File aFile = new File(fileName);
        File pFile = new File(projFile);
        fileName = aFile.getCanonicalPath();

        String layerPathRoot = getPathRoot(aFile);
        String projPathRoot = getPathRoot(pFile);
        if (!layerPathRoot.equalsIgnoreCase(projPathRoot)) {
            RelativePath = fileName;
        } else {
            List<String> aList = new ArrayList<>();
            aList.add(fileName);
            do {
                aList.add("");
                File tempFile = new File(aList.get(aList.size() - 2));
                if (tempFile.exists() && tempFile.getParent() != null) {
                    aList.set(aList.size() - 1, tempFile.getParent());
                } else {
                    break;
                }
            } while (!"".equals(aList.get(aList.size() - 1)));

            List<String> bList = new ArrayList<>();
            bList.add(pFile.getCanonicalPath());
            do {
                bList.add("");
                File tempFile = new File(bList.get(bList.size() - 2));
                if (tempFile.getParent() != null) {
                    bList.set(bList.size() - 1, tempFile.getParent());
                } else {
                    break;
                }
            } while (!"".equals(bList.get(bList.size() - 1)));

            boolean ifExist = false;
            int offSet;
            for (int i = 0; i < aList.size(); i++) {
                for (int j = 0; j < bList.size(); j++) {
                    if (aList.get(i).equals(bList.get(j))) {
                        for (int k = 1; k < j; k++) {
                            RelativePath = RelativePath + ".." + File.separator;
                        }
                        if (aList.get(i).endsWith(File.separator)) {
                            offSet = 0;
                        } else {
                            offSet = 1;
                        }
                        RelativePath = RelativePath + fileName.substring(aList.get(i).length() + offSet);
                        ifExist = true;
                        break;
                    }
                }
                if (ifExist) {
                    break;
                }
            }
        }

        if ("".equals(RelativePath)) {
            RelativePath = fileName;
        }
        return RelativePath;
    }

    /**
     * Convert Image to BufferedImage
     *
     * @param image The Image
     * @return The BufferedImage
     */
    public static BufferedImage imageToBufferedImage(Image image) {

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return bufferedImage;

    }

    /**
     * Make a color of a image transparent
     *
     * @param im The image
     * @param color The color
     * @return Result image
     */
    public static Image makeColorTransparent(BufferedImage im, final Color color) {
        ImageFilter filter = new RGBImageFilter() {
            // the color we are looking for... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFF000000;

            @Override
            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    /**
     * Get application path by a class
     *
     * @param cls Class
     * @return Application path
     */
    public static String getAppPath(Class cls) {
        if (cls == null) {
            throw new java.lang.IllegalArgumentException("The parameter can not be null!");
        }

        ClassLoader loader = cls.getClassLoader();
        //Get class name
        String clsName = cls.getName() + ".class";
        //Get package
        Package pack = cls.getPackage();
        String path = "";
        if (pack != null) {
            String packName = pack.getName();
            //Judge if is Java base class to avoid this condition
            if (packName.startsWith("java.") || packName.startsWith("javax.")) {
                throw new java.lang.IllegalArgumentException("Dont use Java system class!");
            }
            //Remove package name from the class name
            clsName = clsName.substring(packName.length() + 1);
            //Convert package name to path if the package has simple name
            if (!packName.contains(".")) {
                path = packName + "/";
            } else {    //Convert package name to path
                int start = 0;
                int end = packName.indexOf(".");
                while (end != -1) {
                    path = path + packName.substring(start, end) + "/";
                    start = end + 1;
                    end = packName.indexOf(".", start);
                }
                path = path + packName.substring(start) + "/";
            }
        }

        //Get resource
        java.net.URL url = loader.getResource(path + clsName);
        //Get path
        String realPath = url.getPath();
        //Remove "file:"
        int pos = realPath.indexOf("file:");
        if (pos > -1) {
            realPath = realPath.substring(pos + 5);
        }
        //Remove class info
        pos = realPath.indexOf(path + clsName);
        realPath = realPath.substring(0, pos - 1);
        //Remove JAR package name
        if (realPath.endsWith("!")) {
            realPath = realPath.substring(0, realPath.lastIndexOf("/"));
        }

        //Get Chinese path by decode
        try {
            realPath = java.net.URLDecoder.decode(realPath, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return realPath;
    }

    /**
     * String pad left
     *
     * @param str The string
     * @param length Pad length
     * @param ch Pad char
     * @return Padded string
     */
    public static String padLeft(String str, int length, char ch) {
        for (int i = str.length(); i < length; i++) {
            str = ch + str;
        }

        return str;
    }

    /**
     * String pad right
     *
     * @param str The string
     * @param length Pad length
     * @param ch Pad char
     * @return Padded string
     */
    public static String padRight(String str, int length, char ch) {
        for (int i = str.length(); i < length; i++) {
            str = str + ch;
        }

        return str;
    }

    /**
     * Deep clone object
     *
     * @param oldObj Old object
     * @return Cloned object
     * @throws Exception
     */
    public static Object deepCopy(Object oldObj) throws Exception {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos
                    = new ByteArrayOutputStream(); // A
            oos = new ObjectOutputStream(bos); // B
            // serialize and pass the object
            oos.writeObject(oldObj);   // C
            oos.flush();               // D
            ByteArrayInputStream bin
                    = new ByteArrayInputStream(bos.toByteArray()); // E
            ois = new ObjectInputStream(bin);                  // F
            // return the new object
            return ois.readObject(); // G
        } catch (Exception e) {
            System.out.println("Exception in ObjectCloner = " + e);
            throw (e);
        } finally {
            oos.close();
            ois.close();
        }
    }

    /**
     * Deep clone a BufferedIamge
     *
     * @param bi Original image
     * @return Cloned image
     */
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Get default font name
     *
     * @return Default font name
     */
    public static String getDefaultFontName() {
        String[] fontnames = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        List<String> fns = Arrays.asList(fontnames);
        String fn = "宋体";
        if (!fns.contains(fn)) {
            fn = "Arial";
        }

        if (!fns.contains(fn)) {
            fn = fontnames[0];
        }

        return fn;
    }

    /**
     * Get separator
     *
     * @param line The string line
     * @return Delimiter string
     */
    public static String getDelimiter(String line) {
        String separator = null;
        if (line.contains(",")) {
            separator = ",";
        } else if (line.contains(";")) {
            separator = ";";
        }

        return separator;
    }
    
    /**
     * Get delimiter
     * @param file File
     * @return Delimiter
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static String getDelimiter(File file) throws FileNotFoundException, IOException{
        BufferedReader sr = new BufferedReader(new FileReader(file));
        String line = sr.readLine();
        sr.close();
        return getDelimiter(line);        
    }

    /**
     * Split a string line by separator
     *
     * @param line The string line
     * @param separator The separator
     * @return Splitted string array
     */
    public static String[] split(String line, String separator) {
        if (separator == null || separator.equals(" ")) {
            return line.split("\\s+");
        } else {
            String[] strs = line.split(separator);
            List<String> r = new ArrayList<>();
            for (String s : strs){
                r.add(s.trim());
            }
            strs = r.toArray(new String[1]);
            return strs;
        }
    }
    
    /**
     * Capitalize the first character of a string
     * @param str The string
     * @return Capitalized string
     */
    public static String capitalize(String str){        
        if(str == null || str.length() == 0)
            return "";
        
        if(str.length() == 1)
            return str.toUpperCase();
        
        char[] charArray = str.toCharArray();
        charArray[0] = Character.toUpperCase(charArray[0]);            
        return new String(charArray);    
    }
    // </editor-fold>
    
    /**
     * Determine if a string is digital
     *
     * @param strNumber the string
     * @return Boolean
     */
    public static boolean isNumeric(String strNumber) {
        try {
            Double.parseDouble(strNumber);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Create values by interval
     *
     * @param min Miminum value
     * @param max Maximum value
     * @param interval Interval value
     * @return Value array
     */
    public static double[] getIntervalValues(double min, double max, double interval) {
        double[] cValues;
        min = BigDecimalUtil.add(min, interval);
        double mod = BigDecimalUtil.mod(min, interval);
        min = BigDecimalUtil.sub(min, mod);
        int cNum = (int) ((max - min) / interval) + 1;
        int i;

        cValues = new double[cNum];
        for (i = 0; i < cNum; i++) {
            cValues[i] = BigDecimalUtil.add(min, BigDecimalUtil.mul(i, interval));
        }

        return cValues;
    }

    /**
     * Get interval values
     *
     * @param min Minimum value
     * @param max Maximum value
     * @param n Level number
     * @return Values
     */
    public static double[] getIntervalValues(double min, double max, int n) {
        int aD, aE;
        double range;
        String eStr;

        range = BigDecimalUtil.sub(max, min);
        if (range == 0.0) {
            return new double[]{min};
        }

        eStr = String.format("%1$E", range);
        aD = Integer.parseInt(eStr.substring(0, 1));
        aE = (int) Math.floor(Math.log10(range));
        while (n > aD) {
            aD = aD * 10;
            aE = aE - 1;
        }
        double interval = BigDecimalUtil.mul((int) (aD / n), Math.pow(10, aE));

        return getIntervalValues(min, max, interval);
    }
    
    /**
     * Create contour values by minimum and maximum values
     *
     * @param min Minimum value
     * @param max Maximum value
     * @param isExtend If extend values
     * @return Contour values
     */
    public static List<Object> getIntervalValues(double min, double max, boolean isExtend) {
        int i, cNum, aD, aE;
        double cDelt, range, newMin;
        String eStr;
        List<Object> r = new ArrayList<>();

        range = BigDecimalUtil.sub(max, min);
        if (range == 0.0) {
            r.add(new double[]{min});
            r.add(0.0);
            return r;
        } else if (range < 0) {
            range = -range;
            double temp = min;
            min = max;
            max = temp;
        }

        eStr = String.format("%1$E", range);
        aD = Integer.parseInt(eStr.substring(0, 1));
        aE = (int) Math.floor(Math.log10(range));
//        int idx = eStr.indexOf("E");
//        if (idx < 0) {
//            aE = 0;
//        } else {
//            aE = Integer.parseInt(eStr.substring(eStr.indexOf("E") + 1));
//        }
        if (aD > 5) {
            //cDelt = Math.pow(10, aE);
            cDelt = BigDecimalUtil.pow(10, aE);
            cNum = aD;
            //newMin = Convert.ToInt32((min + cDelt) / Math.Pow(10, aE)) * Math.Pow(10, aE);
            //newMin = (int) (min / cDelt + 1) * cDelt;
        } else if (aD == 5) {
            //cDelt = aD * Math.pow(10, aE - 1);
            cDelt = aD * BigDecimalUtil.pow(10, aE - 1);
            cNum = 10;
            //newMin = Convert.ToInt32((min + cDelt) / Math.Pow(10, aE)) * Math.Pow(10, aE);
            //newMin = (int) (min / cDelt + 1) * cDelt;
            cNum++;
        } else {
            //cDelt = aD * Math.pow(10, aE - 1);
            double cd = BigDecimalUtil.pow(10, aE - 1);
            //cDelt = BigDecimalUtil.mul(aD, cDelt);
            cDelt = BigDecimalUtil.mul(5, cd);
            cNum = (int) (range / cDelt);
            if (cNum < 5) {
                cDelt = BigDecimalUtil.mul(2, cd);
                cNum = (int) (range / cDelt);
                if (cNum < 5) {
                    cDelt = BigDecimalUtil.mul(1, cd);
                    cNum = (int) (range / cDelt);
                }
            }
            //newMin = Convert.ToInt32((min + cDelt) / Math.Pow(10, aE - 1)) * Math.Pow(10, aE - 1);
            //newMin = (int) (min / cDelt + 1) * cDelt;            
        }
        int temp = (int) (min / cDelt + 1);
        newMin = BigDecimalUtil.mul(temp, cDelt);
        if (newMin - min >= cDelt) {
            newMin = BigDecimalUtil.sub(newMin, cDelt);
            cNum += 1;
        }

        if (newMin + (cNum - 1) * cDelt > max) {
            cNum -= 1;
        } else if (newMin + (cNum - 1) * cDelt + cDelt < max) {
            cNum += 1;
        }

        //Get values
        List<Double> values = new ArrayList<>();
        double v;
        for (i = 0; i < cNum; i++) {
            v = BigDecimalUtil.add(newMin, BigDecimalUtil.mul(i, cDelt));
            if (v >= min && v <= max)
                values.add(v);
        }

        //Extend values
        if (isExtend) {
            if (values.get(0) > min) {
                values.add(0, BigDecimalUtil.sub(newMin, cDelt));
            }
            if (values.get(values.size() - 1) < max) {
                values.add(BigDecimalUtil.add(values.get(values.size() - 1), cDelt));
            }
        }

        double[] cValues = new double[values.size()];
        for (i = 0; i < values.size(); i++) {
            cValues[i] = values.get(i);
        }

        r.add(cValues);
        r.add(cDelt);
        return r;
    }
    
    /**
     * Create contour values by minimum and maximum values
     *
     * @param min Minimum value
     * @param max Maximum value
     * @return Contour values
     */
    public static double[] getIntervalValues(double min, double max) {
        return (double[]) getIntervalValues(min, max, false).get(0);
    }
}
