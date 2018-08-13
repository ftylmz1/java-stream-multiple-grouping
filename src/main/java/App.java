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

    public static void writeFile(String path) {

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

        //this my source, It will be fetched file or database, it not important
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



        //For example, I will do same job as
        // client - content
        // client - location
        // content - location

        rows.collect(Collectors.toMap(
                g -> Arrays.asList(g.getClient(), g.getContent()),
                v -> {
                    Row nr = new Row();
                    nr.setClient(v.getClient());
                    nr.setContent(v.getContent());
                    nr.setConsumption(v.getConsumption());

                    return nr;
                },
                (t, u) -> {
                    t.setConsumption(t.getConsumption() + u.getConsumption());
                    return t;
                }))
                .values()
                .forEach(f -> {
                    System.out.format("%s|%s|%d\n", f.getClient(), f.getContent(), f.getConsumption());

                });


    }

    public static void main(String[] args) throws Exception {
        //args[0] path to create file
        //args[1] mode: write or read
        //firstly, give path and mode, to create file
        //secondly give path and mode, ro read file that you created to mapreduce process


        if (args[1].equals("write"))
            writeFile(args[0]);
        else if (args[1].equals("read"))
            readFile(args[0]);
    }
}
