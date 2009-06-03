
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.SortedSet;
import java.io.FileFilter;
import javax.swing.JDialog;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.awt.Graphics;

public class FileRenamer extends JDialog implements DocumentListener,  ActionListener  {

    JTextArea renameArea;
    JTextArea fileArea;

    public class DateOnEntry {
        HashMap <String,Long>dateOnEntry=new HashMap<String,Long>();

        public long getDateOnEntry(File file) {
            String path = file.getAbsolutePath();
            if (dateOnEntry.containsKey(path)) {
                return dateOnEntry.get(path);
            }
            else {
                long lastModified = file.lastModified();
                dateOnEntry.put(path, lastModified);
                return lastModified;
            }
        }
    }
    public DateOnEntry dateOnEntry = new DateOnEntry();

    static File startDir = new File(System.getProperty("user.dir"));
    /**
     * Main method.
     */
    public static void main(String s[]) throws Exception {
        System.out.println("STARTING");
        
        FileRenamer fr = new FileRenamer();
        if (startDir.exists())
            fr.setDir(startDir);
        fr.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.out.println("Exiting");
                    System.exit(0);
                }
            });

        fr.show();
    }

    public void fileGuess() {
        Vector destination = createSetFromText(renameArea.getText());
        Vector source = createSetFromText(fileArea.getText());
        HashMap claimedFiles = new HashMap(); // map of dest->source
        HashMap scores = new HashMap();       // map of dest->score
	
        Vector newFileVector = new Vector();

        Iterator destinationIter = destination.iterator();
        int score=0;

        String sourceString=null;

        while (destinationIter.hasNext()) {
            int bestScore=0;
            String bestSource=null;
            int claimedScore=0;
            String destinationString = (String)destinationIter.next();

            // 	    System.out.println("Source: " + sourceString);

            Iterator sourceIter = source.iterator();
            while (sourceIter.hasNext()) {
                sourceString = (String)sourceIter.next();

                // 		System.out.println("    Destination: " + destinationString);

                score = getWordScore(upperFile(destinationString), upperFile(sourceString));

                claimedScore=0;
                if (scores.containsKey(destinationString)) {
                    claimedScore = ((Integer)scores.get(destinationString)).intValue();
                }

                // 		System.out.println("        Score:     " + score);
                // 		System.out.println("        ClaimedScore: " + claimedScore);
                if (score > claimedScore
                    && score > bestScore) {
                    bestScore = score;
                    bestSource = sourceString;
                }
            }

            if (bestScore > 0) {
                removeClaimed(claimedFiles, bestSource);
                // 		System.out.println("    PUT : " + destinationString + " -> " + bestSource);
                claimedFiles.put(destinationString, bestSource);
                scores.put(destinationString, new Integer(bestScore));
            }

            // 	    System.out.println();
        }
        // 	    System.out.println();
	
        Iterator iter3 = claimedFiles.entrySet().iterator();
        while (iter3.hasNext()) {
            Map.Entry entry = (Map.Entry)iter3.next();
            String thisKey = (String)entry.getKey();
            String thisValue = (String)entry.getValue();
            // 	    System.out.println(thisKey + "->" + thisValue);
        }

        Iterator iter2 = destination.iterator();
        while (iter2.hasNext()) {
            String file = (String)iter2.next();
            if (claimedFiles.containsKey(file)) {
                newFileVector.add(claimedFiles.get(file));
            }
            else {
                newFileVector.add("----");
            }
        }

        fileArea.setText(createTextFromSet(newFileVector));

    }


    public void removeClaimed(HashMap claimed, String source) {
        Iterator iter = claimed.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
	    
            String string = (String)(entry.getValue());
            if (string.equals(source)) {
                // 		System.out.println("    REMOVED : " + entry.getKey() + " -> " + source);
		
                claimed.remove(entry.getKey());
                return;
            }
        }
    }
    public int getWordScore(String inSource, String inDestination) {
        int count = 0;

        String source = inSource.replaceAll(".mp3", "");
        String destination = inDestination.replaceAll(".mp3", "");

        /* First create a vector of all the words in source */
        Vector sourceVector = new Vector();
        StringTokenizer sourceToke = new StringTokenizer(source, " _.-/");
        while (sourceToke.hasMoreElements()) {
            String word = sourceToke.nextToken();
            sourceVector.add(word);
        }
	

        /* Now count how many words in destination are in source */
        StringTokenizer toke = new StringTokenizer(destination, " _.-");
        while (toke.hasMoreElements()) {
            String word = toke.nextToken();
            if (sourceVector.contains(word))
                count++;
        }
        return count;
    }

    public FileRenamer() {

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setPreferredSize(new Dimension(800,400));

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        JPanel topPanel = new JPanel(new GridBagLayout());

        fileArea = new JTextArea("");
        renameArea = new JTextArea("");
        renameArea.getDocument().addDocumentListener(this);

        ScrollPane fileScroll = new ScrollPane();
        ScrollPane renameScroll = new ScrollPane();
	

        JButton fileButton = new JButton("Select Directory");
        JButton formatButton = new JButton("Format");
        JButton renumberButton = new JButton("Renumber");
        JButton copyButton = new JButton("Copy from Left");
        JButton deleteButton = new JButton("Delete Rectangle");
        JButton renameButton = new JButton("Rename Files");
        JButton upButton = new JButton("Up");
        JButton downButton = new JButton("Down");
        JButton fileGuessButton = new JButton("File Guess");
        JButton replaceButton = new JButton("Replace String");
        JButton nextDirectoryName = new JButton("Next Directory By Name");
        JButton nextDirectoryDate = new JButton("Next Directory By Date");

        fileButton.addActionListener(this);
        formatButton.addActionListener(this);
        renumberButton.addActionListener(this);
        copyButton.addActionListener(this);
        deleteButton.addActionListener(this);
        renameButton.addActionListener(this);
        upButton.addActionListener(this);
        downButton.addActionListener(this);
        fileGuessButton.addActionListener(this);
        replaceButton.addActionListener(this);
        nextDirectoryName.addActionListener(this);
        nextDirectoryDate.addActionListener(this);

        Border raisedBevel, loweredBevel, border;

        raisedBevel = BorderFactory.createRaisedBevelBorder();
        loweredBevel = BorderFactory.createLoweredBevelBorder();
        border = BorderFactory.createCompoundBorder(raisedBevel,
                                                    loweredBevel);

        fileArea.setBorder(border);
        fileArea.setEditable(false);
        fileArea.setBackground(Color.lightGray);
        fileArea.setForeground(Color.black);

        renameArea.setBorder(border);
        GridBagConstraints c = new GridBagConstraints();

        c.fill=GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;

        fileScroll.add(fileArea);
        renameScroll.add(renameArea);

        c.gridx=0;
        c.gridy=0;
        topPanel.add(fileScroll,c);

        c.gridx++;
        topPanel.add(renameScroll,c);

        c.weightx=1.0;
        c.weighty=1.0;
        c.fill = GridBagConstraints.BOTH;

        c.gridx=0;
        c.gridy=0;
        buttonPanel.add(fileButton,c);

        c.gridx++;
        buttonPanel.add(upButton,c);

        c.gridx++;
        buttonPanel.add(formatButton,c);

        c.gridx++;
        buttonPanel.add(replaceButton,c);

        c.gridx++;
        buttonPanel.add(fileGuessButton,c);

        c.gridx++;
        buttonPanel.add(nextDirectoryName,c);

        c.gridx=0;
        c.gridy++;
        buttonPanel.add(copyButton,c);

        c.gridx++;
        buttonPanel.add(downButton,c);

        c.gridx++;
        buttonPanel.add(renumberButton,c);

        c.gridx++;
        buttonPanel.add(deleteButton,c);

        c.gridx++;
        buttonPanel.add(renameButton,c);

        c.gridx++;
        buttonPanel.add(nextDirectoryDate,c);

        c.fill=GridBagConstraints.BOTH;
        c.weightx=1;
        c.weighty=1;
        c.gridx=0;
        c.gridy=0;
        mainPanel.add(topPanel, c);

        c.fill=GridBagConstraints.NONE;
        c.weightx=0;
        c.weighty=0;
        c.gridx=0;
        c.gridy++;
        mainPanel.add(buttonPanel, c);

        this.getContentPane().add(mainPanel);

        this.pack();
    }

    public int getColumn(String text, int caretPosition) {
        int currentPosition=caretPosition;
        boolean done = false;

        Vector v = createSetFromText(text);

        Iterator i = v.iterator();
        while (i.hasNext()) {
            String s = (String)i.next();
            if ((currentPosition - (s.length()+1)) < 0) {
                return currentPosition;
            }
            currentPosition -= s.length() + 1;
        }
        return currentPosition;
    }

    public String upperFile(String text) {
        String s="";
        int upper = 'A' - 'a';

        boolean shouldUpper = true;

        for (int i=0; i<text.length(); i++) {
            char letter = text.charAt(i);
            if (letter >= 'A' && letter <='Z') {
                letter -= upper;
            }
            if (shouldUpper
                && (i < (text.length() -4))
                && (letter >= 'a' && letter <='z')) {

                letter += upper;
                shouldUpper = false;
            }
            if ((letter < 'A' || letter >'Z')
                && (letter < 'a' || letter >'z')) {
                shouldUpper = true;
            }
            s+=letter;

        }
        return s;
    }

    public String deleteRectangle(String text,
                                  int startX, int startY,
                                  int endX, int endY) {
        Vector lines = createSetFromText(text);
        Vector newLines = new Vector();

        for (int index=0; index < lines.size(); index++) {
            String s = (String)lines.get(index);

            if (startY <= index && index <=endY)
                s = s.substring(0, startX) + s.substring(endX);

            newLines.add(s);
        }
        return createTextFromSet(newLines);
    }
    public TextDef moveRows(TextDef textDef,
                            boolean up) {

        Vector rows = createSetFromText(textDef.text);

        int startRow = textDef.getStartRow();
        int endRow = textDef.getEndRow();

        int start = textDef.start;
        int end = textDef.end;

        if ((up && startRow <= 0)
            || (!up && endRow >= rows.size()))

            return textDef;

        String moveRow;
        if (up) {
            moveRow= (String)rows.remove(startRow-1);
            rows.insertElementAt(moveRow, endRow);

            start -= moveRow.length()+1;
            end -= moveRow.length()+1;
        }
        else {
            moveRow= (String)rows.remove(endRow+1);
            rows.insertElementAt(moveRow, startRow);

            start += moveRow.length()+1;
            end += moveRow.length()+1;
        }

        return new TextDef(createTextFromSet(rows),
                           start,
                           end);
    }

    private class TextDef {
        public String text;
        public int start;
        public int end;

        public TextDef(String text, int start, int end) {
            this.text = text;
            this.start = start;
            this.end = end;
        }

        public int getStartRow() {
            return getRow(text, start);
        }
        public int getEndRow() {
            return getRow(text, end);
        }
    }
    public int getRow(String text, int caretPosition) {
        int currentPosition=caretPosition;
        int rowCount=0;
        Vector v = createSetFromText(text);

        Iterator i = v.iterator();
        while (i.hasNext()) {
            String s = (String)i.next();
            if ((currentPosition - (s.length()+1)) < 0) {
                return rowCount;
            }
            currentPosition -= s.length() + 1;
            rowCount++;
        }
        return rowCount;
    }

    /**
     *
     * @param param1 <description>
     */
    public void actionPerformed(ActionEvent param1) {
        String command = param1.getActionCommand();
        if (command.equals("File Guess")) {
            // 	    System.out.println("Guess File");
            fileGuess();
        }
        if (command.equals("Replace String")) {
            System.out.println("Replace string");
            ReplaceParameters params = showReplaceDialog(renameArea.getSelectedText());
            String newText = renameArea.getText().replaceAll(params.replace,
                                                             params.with);
            renameArea.setText(newText);
        }
        if (command.equals("Next Directory By Name")) {
            System.out.println("Next Directory Name");
            File directory = findNextDirectory(true, startDir, startDir);
            setDir(directory);
        }
        if (command.equals("Next Directory By Date")) {
            System.out.println("Next Directory");
            File directory = findNextDirectory(false, startDir, startDir);
            setDir(directory);
        }
        if (command.equals("Up")
            || command.equals("Down")) {

            boolean up = command.equals("Up");

            int start = renameArea.getSelectionStart();
            int end = renameArea.getSelectionEnd();

            TextDef textDef = new TextDef(renameArea.getText(),
                                          start, end);
            TextDef result = moveRows(textDef, up);
	    

            renameArea.setText(result.text);
            renameArea.setSelectionStart(result.start);
            renameArea.setSelectionEnd(result.end);
        }
        if (param1.getActionCommand().equals("Delete Rectangle")) {
            int start = renameArea.getSelectionStart();
            int end = renameArea.getSelectionEnd();

            int startX = getColumn(renameArea.getText(), start);
            int startY = getRow(renameArea.getText(), start);
            int endX = getColumn(renameArea.getText(), end);
            int endY = getRow(renameArea.getText(), end);

            if (startX > endX
                || startY > endY)
                return;
	    
            String result = deleteRectangle(renameArea.getText(),
                                            startX, startY, endX, endY);
            renameArea.setText(result);
        }
        if (param1.getActionCommand().equals("Rename Files")) {
            String message;
            Vector sourceFiles = createSetFromText(fileArea.getText());
            Vector destinationFiles = createSetFromText(renameArea.getText());

            if (sourceFiles.size() != destinationFiles.size()) {
                message = "Not the same number of files on left (" + sourceFiles.size()
                    + ") as right(" + destinationFiles.size() + ")";

                JOptionPane.showMessageDialog(this,
                                              message,
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);

                return;
            }
            for (int i=0; i<sourceFiles.size(); i++) {
                File source = new File("" + startDir + File.separatorChar
                                       + sourceFiles.get(i));

                File destination = new File("" + startDir + File.separatorChar
                                            + (String)destinationFiles.get(i));
		
                message = null;
                if (!source.exists()) {
                    // 		    message = "Source file " + source.getName() + " does not exist";
                    continue;
                }
                if (destination.exists()
                    && !source.equals(destination)) {

                    message = "Attempt to overwrite file";
                }
                if (message != null) {
                    JOptionPane.showMessageDialog(this,
                                                  message,
                                                  "Error",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                }


                if (!source.equals(destination)) {
                    boolean success=source.renameTo(destination);
                    if (!success) {
                        System.out.println("Failed to rename file");
                    }
                }
            }
            setDir(startDir);
            renameArea.setText("");
        }

        if (param1.getActionCommand().equals("Format")) {
            int line = 0;

            System.out.println("in format");
        
            Vector lines =  createSetFromText(renameArea.getText());
            Vector newLines = new Vector();

            Iterator iter = lines.iterator();
            while (iter.hasNext()) {

                String s = (String)iter.next();
                s = s.trim();
                // 		s = renumber(s, ++line);
                s = fixNumber(s, ++line);
		
		
//                 if (s.endsWith(".m3u"))
//                     continue;

                //                 Pattern p = Pattern.compile(".*Listen\\s*Listen");
                //                 Matcher m = p.matcher(s);
                //                 boolean b = m.matches();
                //                 if (b)
                //                     System.out.println("matches");
                //                 else
                //                     System.out.println("DOESN'T match");

                String[] result = s.split("ListenMusic\\s*ListenMusic");

                result = s.split("\\s*ListenMusic");
                if (result.length > 0) {
                    s = result[0];
                }
                // 		s = s.trim();
                
//                 if (s.endsWith(".MP3"))
//                     s=s.substring(0,s.length()-4) + ".mp3";

//                 if (!s.endsWith(".mp3"))
//                     s=s+".mp3";

                    
                s=replace(s,' ', '_');
                s=replace(s,'\'', '\0');
                s=replace(s,'/', '-');
                s=replace(s,',', '\0');
                s=replace(s,'!', '\0');
		
                s = upperFile(s);
                newLines.add(s);
            }
            renameArea.setText(createTextFromSet(newLines));
        }
        if (param1.getActionCommand().equals("Renumber")) {
            int line = 0;

            Vector lines =  createSetFromText(renameArea.getText());
            Vector newLines = new Vector();

            Iterator iter = lines.iterator();
            while (iter.hasNext()) {

                String s = (String)iter.next();
                s = s.trim();
                s = renumber(s, ++line);

                newLines.add(s);
            }
            renameArea.setText(createTextFromSet(newLines));
        }
        if (param1.getActionCommand().equals("Select Directory")) {
            JFileChooser chooser = new JFileChooser(startDir);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: " +
                                   chooser.getSelectedFile().getName());
                startDir = chooser.getSelectedFile();
                setDir(startDir);
            }
        }
        if (param1.getActionCommand().equals("Copy from Left")) {
            renameArea.setText(fileArea.getText());
        }
    }

    public void setDir(File startDir){
        this.setTitle(startDir.getAbsolutePath());
        TreeSet files = new TreeSet();

        dateOnEntry.getDateOnEntry(startDir);
        
        this.startDir = startDir;

        Iterator iter = Arrays.asList(startDir.list()).iterator();
        while (iter.hasNext()) {
            
            String filename = (String)iter.next();
            File f = new File(filename);
            if (!f.isDirectory())
                files.add(filename);
        }
        fileArea.setText(createTextFromSet(files));
    }

    public class FileDateComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if (o1 instanceof File && o2 instanceof File)
                return compare((File)o1, (File)o2);
            if (o1 instanceof File && o2 instanceof Long)
                return compare((File)o1, (((Long)o2)).longValue());
            if (o1 instanceof Long && o2 instanceof File)
                return compare(((Long)o1).longValue(), (File)o2);
            if (o1 instanceof Long && o2 instanceof Long)
                return compare(((Long)o1).longValue(), ((Long)o2).longValue());
            System.out.println("Wasn't expecting arguments of class "
                               + o1.getClass() + " and " + o2.getClass());
            return -1;
            
        }
        public int compare(long f1, long f2) {
            return ((Long)f1).compareTo(f2);
//             System.out.println("comparing "
//                                + f1.lastModified() + " - " + f2.lastModified()
//                                + " = " + timeDiff + f1 + " " + f2);
        }            

        public int compare(File f1, long f2) {
            return compare(f1.lastModified(), f2);
        }
        public int compare(long f1, File f2) {
            return compare(f1, f2.lastModified());
        }
        public int compare(File f1, File f2) {
            int result = compare(f1.lastModified(), f2.lastModified());
            if (result == 0)
                return f1.compareTo(f2);
            return result;
        }

        public boolean equals(File f1, long f2) {
            return f2 == f1.lastModified();
        }

        public boolean equals(File f1, File f2) {
            return f2.lastModified() == f1.lastModified();
        }
    }
    public class FileDateFilter
        extends FileDateComparator
        implements FileFilter
    {
        long greaterThanThisDate;

        public FileDateFilter(long date) {
            this.greaterThanThisDate = date;
        }
        public boolean accept(File f2) {
            if (compare(f2, greaterThanThisDate) >= 0)
                return true;
            return false;
        }
    }

    public File findNextDirectory(boolean byName,
                                  File parentDir,
                                  File currentDir) {
        File[] files;

        // if we're searching by name, or this is the first time, just list
        // the files.
        if (parentDir == null) {
            System.out.println("Couldn't find newer directory");
            return currentDir;
        }

        if (byName || parentDir.equals(currentDir)) {
            files = parentDir.listFiles();
        }
        // if we have a parent dir and we're searching by date, use the date
        // of the current dir
        else {
            long date = dateOnEntry.getDateOnEntry(currentDir);
            files = parentDir.listFiles(new FileDateFilter(date));
        }

        Comparator<File> comparator;

        TreeSet sortedFiles;
        SortedSet <File> sortedSet;

        if (byName)
            sortedFiles = new TreeSet<File>();
        else { 
            FileDateComparator compare = new FileDateComparator();
            sortedFiles = new TreeSet(compare);
        }

        sortedFiles.addAll(Arrays.asList(files));

//         SortedSet set = sortedFiles.tailSet(sortedFiles.first());
//         for (Object o : set) {
//             System.out.println(((File)o).lastModified() + " " + o);
//         }

        // first time through.  Use all subdirectories.
        if (parentDir.equals(currentDir)) {
            sortedSet = sortedFiles.tailSet(sortedFiles.first());
        }
        // We moved up to a parent directory.  Need to get a tail set from the
        // current directory, then remove the current dir.
        else {
            long date = dateOnEntry.getDateOnEntry(currentDir);
            sortedSet = sortedFiles.tailSet(date);
            sortedSet.remove(currentDir);
        }
        for (File f : sortedSet) {
            if (f.isDirectory()) {
                return f;
            }
        }

        // if we got here, there were no subdirectories.  We need to go up one
        // and try again
        System.out.println("Moving up");

        return findNextDirectory(byName,
                                 parentDir.getParentFile(),
                                 parentDir);
        
    }

    public String renumber(String s, int count) {
        // 	int index = s.indexOf('.');
        // 	if (s.endsWith(".mp3")) {
        // 	    if (index != s.indexOf(".mp3"))
        // 		s=s.substring(index+1);
        // 	}
        // 	else
        Pattern p = Pattern.compile("^(\\d )*.");
        Matcher m = p.matcher(s);
        if (m.matches()) {
            int index = s.indexOf('.');
            s=s.substring(index+1);
        }

        s=s.trim();
        if (count < 10)
            s = "0" + count + "." + s;
        else
            s = count + "." + s;

        return s;
    }

    public String fixNumber(String s, int count) {
        String newString = s;

        // if number dot space
        if (s.charAt(0) >= '0'
            && s.charAt(0) <= '9'
            && s.charAt(1) == '.') {

            newString = "0" + s.charAt(0) + ".";

            int index = 1;
            boolean done = false;
            while (!done) {
                char c = s.charAt(index);
                if (c == ' ' || c == '-' || c == '.')
                    index++;
                else
                    done = true;
            }
            newString += s.substring(index);
        }
        // or number number dot
        else if (s.charAt(0) >= '0'
                 && s.charAt(0) <= '9'
                 && s.charAt(1) >= '0'
                 && s.charAt(1) <= '9') {

            newString = s.substring(0,2) + ".";

            int index = 2;
            boolean done = false;
            if (s.length() <=2)
                done = true;
            while (!done) {
                char c = s.charAt(index);
                if (c == ' ' || c == '-' || c == '.')
                    index++;
                else
                    done = true;
            }
            newString += s.substring(index);
        }


        return newString;
    }



    public String replace(String s, char old, char newChar) {
        if (newChar != (char)0) {
            return s.replace(old, newChar);
        }
        else {
            int index;

            while ((index = s.indexOf(old)) != -1) {
                s = s.substring(0, index) + s.substring(index+1);
            }
	    
        }
        return s;
    }

    public String trim(String text) {
        String result = "";

        StringTokenizer toke = new StringTokenizer(text, "\n");
        while (toke.hasMoreElements()) {
            String s = (String)toke.nextElement();
            s.trim();
            result += s + "\n";
        }
        return result;
    }

    // implementation of javax.swing.event.DocumentListener interface

    /**
     *
     * @param param1 <description>
     */
    public void insertUpdate(DocumentEvent param1)
    {
        // 	renameArea.setText(trim(renameArea.getText()));
    }

    /**
     *
     * @param param1 <description>
     */
    public void removeUpdate(DocumentEvent param1)
    {
    }

    /**
     *
     * @param param1 <description>
     */
    public void changedUpdate(DocumentEvent param1)
    {
    }

    public Vector createSetFromText(String text) {
        Vector lines = new Vector();

        StringTokenizer toke = new StringTokenizer(text, "\n");
        while (toke.hasMoreElements()) {
            String s = (String)toke.nextElement();
            if (s.length() > 0)
                lines.add(s);
        }
        return lines;
    }
    public String createTextFromSet(Collection lines) {
        String text = "";
        Iterator iter = lines.iterator();
        while (iter.hasNext()) {
            String s = (String)iter.next();
            text += s + "\n";
        }
        return text;
    }

    //      public static void createReport(String s) {
    //          File dir = new File(s);
    //  	File files[] = dir.listFiles();

    //  	if (files == null) {
    //  	    return;
    //  	}
	
    //  	for (int i=0; i<files.length; i++) {
    //  	    if (files[i].isDirectory()) {
    //  		String newDir;
    //  		newDir = files[i].getAbsolutePath();
		
    //  		createReport(newDir);
    //  	    }
    //  	    else {
    //  		System.out.println(files[i].length()
    //  				   + ","
    //  				   + files[i].lastModified()
    //  				   + ","
    //  				   + files[i]);
    //  	    }
    //  	}

    //      }
    public class ReplaceParameters {
        String replace;
        String with;
        ReplaceParameters(String replace, String with) {
            this.replace = replace;
            this.with = with;
        }
    }

    public ReplaceParameters showReplaceDialog(String replaceSelection) {
        JPanel panel = new JPanel(new GridLayout(2,2));
        JTextField replace = new JTextField(replaceSelection);
        JTextField with = new JTextField();

        panel.add(new JLabel("Replace:")); panel.add(replace);
        panel.add(new JLabel("With:"));    panel.add(with);

        JOptionPane.showMessageDialog(this, panel);

        return new ReplaceParameters(replace.getText(),with.getText());
    }

}

