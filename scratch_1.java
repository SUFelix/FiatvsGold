import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
class Scratch {

    static DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    //Es werden insgesamt zwei Files eingelesen(goldpreise_in_euro.txt  und  leitzinsen.txt)
    //Damit dass Program funktioniert müssen die Pfade geändert werden in Zeile 11,12,13,14
    static String path2 ="C:\\Users\\Felix\\Desktop\\Gold_Money_Projects\\leitzinsen.txt";
    static String path = "C:\\Users\\Felix\\Desktop\\Gold_Money_Projects\\goldpreise_in_euro.txt";
    static String path3 = "C:\\Users\\Felix\\Desktop\\Gold_Money_Projects\\data_out.txt";
    static String path4 = "C:\\Users\\Felix\\Desktop\\Gold_Money_Projects\\ir_out.txt";

    public static void main(String[] args) throws IOException, ParseException {


        /*
        values[][] wird in diesem Programm den Großteil der Daten speichern

        Erste Spalte ist für das Datum
        Zweite Spalte für den Goldpreis zum Monatsersten in Euro
        Dritte Spalte für den Hauptrefinanzierungssatz der EZB
        Vierte Spalte für den Gewinn/Verlust des Szenarios
        *
        Länge Array 01.01.1999-01.03.2022 sind 279 Monate + 1 Für die Kopfzeile
          */
        String[][] values  = new String[280][4];
        int i  =0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = "";

            while(null != (line = reader.readLine())){

                //Zeile wird aufgeteilt in Datum und Goldpreis(in Euro)
                String[] helper =  new String[2];
                helper[0] = line.substring(0,11);
                helper[1] = line.substring(11);

                // Datum und Goldpreis werden in das Array gespeichert
                values[i][0] = helper[0];
                values[i][1] = helper[1];

                //Das Datum wird umgewandelt, um dann den Leitzinssatz(MRO) des jeweiligen Datums zu erhalten : siehe Methode getiRate()
                //i=0 ist die Kopfzeile
                if(i!=0)
                {
                    Date datum1 = format.parse(helper[0]);
                    values[i][2] = getiRate(datum1)+"";
                }
                i++;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();



        }


        //jetzt wird der Profit des beschriebenen Szenarios berechnet.
        //bei 1 anfangen, weil 0 "data price" ist
        //bei 268 aufhören, da immer noch 12 einträge höher(1 Jahr später mit berücksichtigt wird)

        for(int j = 1;j<268;j++){

            float gp1 = Float.parseFloat(numberSwap(values[j][1]));
            float gp2 = Float.parseFloat(numberSwap(values[j+12][1]));
            float ir = Float.parseFloat(values[j][2]+"f");

            float profit = calcOneYearProfit(gp1,gp2,ir);
            values[j][3] = runden(profit+"");


        }

        //Die Ergebnisse aus values[][]werden jetzt in ein Textdokument geschrieben, so dass es sich einfach in Exel kopieren lässt, zum erstellen von Diagrammen
        try{
            //Jetzt werden noch zwei Textdateien erstellt, eine mit Profiten zum jeweiligen Datum , das andere mit Hauptrefinanzierungssätzen zum jeweiligen Datum

            File file = new File(path3);
            File file2 = new File(path4);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(file2));
            for(int j = 1;j<268;j++){
                writer.write(values[j][0]+"    "+numberBackSwap(values[j][3]));
                writer.newLine();

                writer2.write(values[j][0]+"    "+numberBackSwap(values[j][2]));
                writer2.newLine();

            }
            writer.close();
            writer2.close();
        }
        catch(Exception e){System.out.print("you failed!");}



    }

    //Für das bestimmen der interrest Rate eines bestimmten Datums benutzen wir das File mit den i rates zum datum der jeweiligen änderung
    //In einer Schleife springen wir so lang zum nächsten Datum/iRate bis das datum für welches wir die irate bestimmen aktueller ist als
    //das Datum der letzten Änderung. Dann verwenden wir die seit der änderung gültige iRate
    public static float getiRate(Date date) throws IOException
    {

        BufferedReader reader = new BufferedReader(new FileReader(path2));
        float lz  = 0.1f;

        String line = "";
        while(null != (line=reader.readLine()))
        {
            String zeilendatum = line.substring(0,10);
            lz = Float.parseFloat(line.substring(11));
            Date date2 = new Date();

            try
            {
                date2 = format.parse(zeilendatum);
            }
            catch (ParseException e)
            {
                System.out.println("Datum parse funktioniert nicht");
            }

            if(date.after(date2))break;
        }

        return lz;
    }

    public static float calcOneYearProfit(float goldpreis1,float goldpreis2,float iRate){
        float kapkosten = iRate+2.0f; // in Prozent
        float kosten = 0.01f*kapkosten*10000.0f; // *0.01f um aus ganzen Prozent eine Dezimalzahl zu machen
        float plus = (goldpreis2/goldpreis1)*10000.0f; // Wert des Goldes nach einem Jahr
        float gewinn = plus-kosten-10000.0f;
        return gewinn;
    }

    //Es folgen drei kleine Helfermethoden die dazu dienen die Verarbeitung mit Exel zu erleichtern
    //In Exel werden Nachkommastellen mit Komma getrennt in Java mit Punkt numberSwap und numberBackSwap ermöglichen die einfache Umwandlung
    //runden(String s) schneidet alle Nachkommastellen ausser der ersten ab, da diese mehr Unübersichtlichkeit als Mehrwert beitragen
    public static String numberSwap(String s){
        char[] array = s.toCharArray();
        String neu = "";
        for(char c : array){
            if(c==',')c='.';
            else if(c=='.')c=' ';
            if(c!=' ')neu = neu+c;
        }
        return neu;
    }

    public static String numberBackSwap(String s){
        char[] array = s.toCharArray();
        String neu = "";
        for(char c : array){
            if(c=='.')c=',';
            if(c!=' ')neu = neu+c;
        }
        return neu;
    }

    public static String runden(String s){
        char[] array = s.toCharArray();
        String neu = "";
        int counter = array.length;
        int punktposition = 0;
        for(int j = 0;j<counter;j++){
            if(array[j]=='.')punktposition=j;
        }
        for(int j = 0;j<punktposition+2;j++){
            neu = neu+array[j];
        }

        return neu;

    }
}