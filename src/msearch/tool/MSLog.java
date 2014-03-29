/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package msearch.tool;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import msearch.daten.MSConfig;
import org.apache.commons.lang3.StringUtils;

public class MSLog {

    public static final int FEHLER_ART_PROG = 0;
    public static final String FEHLER_ART_PROG_TEXT = "   Prog: ";
    public static final int FEHLER_ART_GETURL = 1;
    public static final String FEHLER_ART_GETURL_TEXT = " GetUrl: ";
    public static final int FEHLER_ART_MREADER = 2;
    public static final String FEHLER_ART_MREADER_TEXT = "MReader: ";
    public static final int FEHLER_ART_FILME_SUCHEN = 3;
    public static final String FEHLER_ART_FILME_SUCHEN_TEXT = "  Filme: ";
    // private
    private static LinkedList<Integer[]> fehlerListe = new LinkedList<>(); // [Art, Fehlernummer, Anzahl, Exception(0,1 für ja, nein)]
    private static boolean progress = false;
    private static String progressText = "";
    private static final Date startZeit = new Date(System.currentTimeMillis());
    private static Date stopZeit = null;

    public void resetFehlerListe() {
        fehlerListe.clear();
    }

    public static synchronized void versionsMeldungen(String classname) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        MSLog.systemMeldung("");
        MSLog.systemMeldung("");
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("Programmstart: " + sdf.format(startZeit));
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("##################################################################################");
        long totalMem = Runtime.getRuntime().totalMemory();
        MSLog.systemMeldung("totalMemory: " + totalMem / (1024L * 1024L) + " MiB");
        long maxMem = Runtime.getRuntime().maxMemory();
        MSLog.systemMeldung("maxMemory: " + maxMem / (1024L * 1024L) + " MiB");
        long freeMem = Runtime.getRuntime().freeMemory();
        MSLog.systemMeldung("freeMemory: " + freeMem / (1024L * 1024L) + " MiB");
        MSLog.systemMeldung("##################################################################################");
        //Version
        MSLog.systemMeldung(MSFunktionen.getProgVersionString());
        MSLog.systemMeldung("Compiled: " + MSFunktionen.getCompileDate());
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("Java");
        MSLog.systemMeldung("Classname: " + classname);
        String[] java = MSFunktionen.getJavaVersion();
        for (String ja : java) {
            MSLog.systemMeldung(ja);
        }
        MSLog.systemMeldung("");
        MSLog.systemMeldung("");
    }

    public static synchronized void startMeldungen(String classname) {
        versionsMeldungen(classname);
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("Programmpfad: " + MSFunktionen.getPathJar());
        MSLog.systemMeldung("Filmliste: " + MSConfig.getPathFilmlist(true /*aktDate*/));
        MSLog.systemMeldung("Useragent: " + MSConfig.getUserAgent());
        MSLog.systemMeldung("##################################################################################");
        MSLog.systemMeldung("");
        MSLog.systemMeldung("");
        if (MSConfig.senderAllesLaden) {
            MSLog.systemMeldung("Laden:  alles");
        } else {
            MSLog.systemMeldung("Laden:  nur update");
        }
        if (MSConfig.updateFilmliste) {
            MSLog.systemMeldung("Filmliste:  nur updaten");
        } else {
            MSLog.systemMeldung("Filmliste:  neu erstellen");
        }
        MSLog.systemMeldung("Import (ersetzen):  " + MSConfig.importUrl__ersetzen);
        MSLog.systemMeldung("Import (anhängen):  " + MSConfig.importUrl__anhaengen);
        if (MSConfig.nurSenderLaden != null) {
            MSLog.systemMeldung("Nur Sender laden:  " + StringUtils.join(MSConfig.nurSenderLaden, ','));
        }
        MSLog.systemMeldung("##################################################################################");
    }

    public static synchronized void endeMeldung() {
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("##################################################################################");
        if (fehlerListe.size() == 0) {
            systemMeldung(" Keine Fehler :)");
        } else {
            // Fehler ausgeben
            int i_1;
            int i_2;
            for (int i = 1; i < fehlerListe.size(); ++i) {
                for (int k = i; k > 0; --k) {
                    i_1 = fehlerListe.get(k - 1)[1];
                    i_2 = fehlerListe.get(k)[1];
                    // if (str1.compareToIgnoreCase(str2) > 0) {
                    if (i_1 < i_2) {
                        fehlerListe.add(k - 1, fehlerListe.remove(k));
                    } else {
                        break;
                    }
                }
            }
            Iterator<Integer[]> it = fehlerListe.iterator();
            while (it.hasNext()) {
                Integer[] integers = it.next();
                String z;
                switch (integers[0]) {
                    case FEHLER_ART_MREADER:
                        z = FEHLER_ART_MREADER_TEXT;
                        break;
                    case FEHLER_ART_FILME_SUCHEN:
                        z = FEHLER_ART_FILME_SUCHEN_TEXT;
                        break;
                    case FEHLER_ART_GETURL:
                        z = FEHLER_ART_GETURL_TEXT;
                        break;
                    case FEHLER_ART_PROG:
                        z = FEHLER_ART_PROG_TEXT;
                        break;
                    default:
                        z = "";
                }
                boolean ex = integers[3] == 1;
                String strEx;
                if (ex) {
                    strEx = "Ex! ";
                } else {
                    strEx = "    ";
                }
                if (integers[1] < 0) {
                    systemMeldung(strEx + z + " Fehlernummer: " + integers[1] + " Anzahl: " + integers[2]);
                } else {
                    systemMeldung(strEx + z + " Fehlernummer:  " + integers[1] + " Anzahl: " + integers[2]);
                }
            }
        }
        systemMeldung("##################################################################################");
        // Laufzeit ausgeben
        stopZeit = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        int minuten;
        try {
            minuten = Math.round((stopZeit.getTime() - startZeit.getTime()) / (1000 * 60));
        } catch (Exception ex) {
            minuten = -1;
        }
        systemMeldung("");
        systemMeldung("");
        systemMeldung("##################################################################################");
        systemMeldung("   --> Beginn: " + sdf.format(startZeit));
        systemMeldung("   --> Fertig: " + sdf.format(stopZeit));
        systemMeldung("   --> Dauer[Min]: " + (minuten == 0 ? "<1" : minuten));
        systemMeldung("##################################################################################");
        systemMeldung("");
        systemMeldung("   und Tschuess");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("##################################################################################");
    }

    // Fehlermeldung mit Exceptions
    public static synchronized void fehlerMeldung(int fehlerNummer, int art, String klasse, Exception ex) {
        fehlermeldung_(fehlerNummer, art, klasse, ex, new String[]{});
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, int art, String klasse, Exception ex, String text) {
        fehlermeldung_(fehlerNummer, art, klasse, ex, new String[]{text});
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, int art, String klasse, Exception ex, String text[]) {
        fehlermeldung_(fehlerNummer, art, klasse, ex, text);
    }

    // Fehlermeldungen
    public static synchronized void fehlerMeldung(int fehlerNummer, int art, String klasse, String text) {
        fehlermeldung_(fehlerNummer, art, klasse, null, new String[]{text});
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, int art, String klasse, String[] text) {
        fehlermeldung_(fehlerNummer, art, klasse, null, text);
    }

    public static synchronized void systemMeldung(String[] text) {
        systemmeldung_(text);
    }

    public static synchronized void systemMeldung(String text) {
        systemmeldung_(new String[]{text});
    }

    public static synchronized void debugMeldung(String text) {
        if (MSConfig.debug) {
            resetProgress();
            System.out.println("|||| " + text);
            printProgress();
        }
    }

    public static synchronized void progress(String texte) {
        progress = true;
        progressText = texte + "\r";
        printProgress();
        progress = false;
    }

    private static void printProgress() {
        System.out.print(progressText);
    }

    private static void resetProgress() {
        // Leerzeite um die Progresszeile zu löschen
        System.out.print("                                                                                           \r");
    }

    private static void addFehlerNummer(int nr, int art, boolean exception) {
        Iterator<Integer[]> it = fehlerListe.iterator();
        int ex = exception ? (ex = 1) : (ex = 2);
        while (it.hasNext()) {
            Integer[] i = it.next();
            if (i[1].intValue() == nr) {
                i[0] = art;
                i[2]++;
                i[3] = ex;
                return;
            }
        }
        // dann gibts die Nummer noch nicht
        fehlerListe.add(new Integer[]{new Integer(art), new Integer(nr), new Integer(1), new Integer(ex)});
    }

    private static void fehlermeldung_(int fehlerNummer, int art, String klasse, Exception ex, String[] texte) {
        addFehlerNummer(fehlerNummer, art, ex != null);
        if (ex != null || MSConfig.debug) {
            // Exceptions immer ausgeben
            resetProgress();
            final String FEHLER = "Fehler(" + MSConst.PROGRAMMNAME + "): ";
            String x, z;
            if (ex != null) {
                x = "!";
            } else {
                x = "=";
            }
            switch (art) {
                case FEHLER_ART_MREADER:
                    z = "  ==>";
                    break;
                case FEHLER_ART_FILME_SUCHEN:
                    z = "   >>";
                    break;
                case FEHLER_ART_GETURL:
                    z = "  ++>";
                    break;
                case FEHLER_ART_PROG:
                default:
                    z = "*";
            }
            System.out.println(x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x);

            try {
                // Stacktrace
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    ex.printStackTrace(pw);
                    pw.flush();
                    sw.flush();
                    System.out.println(sw.toString());
                }
            } catch (Exception e) {
            }

            System.out.println(z + " Fehlernr: " + fehlerNummer);
            if (ex != null) {
                System.out.println(z + " Exception: " + ex.getMessage());
            }
            System.out.println(z + " " + FEHLER + klasse);
            for (int i = 0; i < texte.length; ++i) {
                System.out.println(z + "           " + texte[i]);
            }
            System.out.println("");
            printProgress();
        }
    }

    private static void systemmeldung_(String[] texte) {
        resetProgress();
        final String z = ". ";
        if (texte.length <= 1) {
            System.out.println(z + " " + texte[0]);
        } else {
            String zeile = "---------------------------------------";
            String txt;
            System.out.println(z + zeile);
            for (int i = 0; i < texte.length; ++i) {
                txt = "| " + texte[i];
                System.out.println(z + txt);
            }
            System.out.println(z + zeile);
        }
        printProgress();
    }

}
