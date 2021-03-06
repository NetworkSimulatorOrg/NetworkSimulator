import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * https://www.baeldung.com/java-csv was a valuable resource in developing the methods of this class.
 */

public class CSVWriter {
    private String fileName;
    private int bufferSize;
    private File file;
    private PrintWriter writer;
    private boolean fileOpen;
    private ArrayList<String[]> data;

    public CSVWriter(String fileName) {
        this.fileName = fileName;
        this.bufferSize = 30;
        this.data = new ArrayList<>();
        this.createFile();
    }

    public CSVWriter(String filename, int bufferSize) {
        this(filename);
        this.bufferSize = bufferSize;
    }

    public void createFile() {
        file = new File(this.fileName);
        try{
            file.createNewFile();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean openFile() {
        if(!fileOpen) {
            try {
                writer = new PrintWriter(new FileOutputStream(file, true));
                fileOpen = true;
            } catch (FileNotFoundException e) {
                System.out.println(e);
                fileOpen = false;
                return false;
            }
        }
        return true;
    }

    public boolean closeFile() {
        if(fileOpen) {
            bufferWrite(true);
            writer.close();
        }
        return true;
    }

    public synchronized void bufferWrite(boolean force) {
        if((force && data.size() > 0) || data.size() >= bufferSize) {
            if(openFile()) {
                data.stream()
                        .map(this::convertToCSV)
                        .forEach(writer::println);

                data.clear();
            }
        }
    }

    public synchronized void appendLine(String[] line) {
        data.add(line);
        bufferWrite(false);
    }

    public void appendDataAsLine(String ... strings) {
        appendLine(strings);
    }

    public synchronized void appendLines(ArrayList<String[]> lines) {
        data.addAll(lines);
        bufferWrite(false);
    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
