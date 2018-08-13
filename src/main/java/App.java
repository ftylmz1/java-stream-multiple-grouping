import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class App {

    public static final int lineCount = 10000000;

    public static final Random rand = new Random();

    public static void writeFile(String path){

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(path);
            bw = new BufferedWriter(fw);

            final BufferedWriter bw2 = bw;

            IntStream.range(0, lineCount).forEach(f -> {

                try {
                    bw2.write(String.format("%s|%s|%s|%d\n",
                            "Content" + rand.nextInt(5),
                            "Client" + rand.nextInt(4),
                            "Location" + rand.nextInt(3),
                            rand.nextInt(10)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });


            System.out.println("Done");

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }
        }
    }

    public static void readFile(String path) throws Exception {

        Stream<Row> rows = Files.lines(Paths.get(path))
                .map(m -> {

                    String[] splits = m.split("\\|");
                    Row r = new Row();
                    r.setContent(splits[0]);
                    r.setClient(splits[1]);
                    r.setLocation(splits[2]);
                    r.setConsumption(Integer.parseInt(splits[3]));
                    return r;
                });


        System.out.println(rows.collect(Collectors.groupingBy(g-> g.getContent())).size());
        System.out.println(rows.collect(Collectors.groupingBy(g-> g.getClient())).size());
        System.out.println(rows.collect(Collectors.groupingBy(g-> g.getLocation())).size());

        System.out.println(rows.count());
    }

    public static void main(String[] args) throws Exception{
        if(args[1].equals("write"))
            writeFile(args[0]);
        else if(args[1].equals("read"))
            readFile(args[0]);
    }
}
