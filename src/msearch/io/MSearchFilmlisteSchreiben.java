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
package msearch.io;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import msearch.daten.DatenFilm;
import msearch.daten.ListeFilme;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class MSearchFilmlisteSchreiben {

    private XMLOutputFactory outFactory;
    private XMLStreamWriter writer;
    private OutputStreamWriter out = null;
    ZipOutputStream zipOutputStream = null;
    BZip2CompressorOutputStream bZip2CompressorOutputStream = null;
    public void filmlisteSchreibenJson(String datei, ListeFilme listeFilme) {
        MSearchLog.systemMeldung("Filme Schreiben");
        File file = new File(datei);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                MSearchLog.fehlerMeldung(915236478, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteSchreiben.xmlSchreibenStart", "Kann den Pfad nicht anlegen: " + dir.toString());
            }
        }
        MSearchLog.systemMeldung("Start Schreiben nach: " + datei);
        try {
            String sender = "", thema = "";
            JsonFactory jsonF = new JsonFactory();
            JsonGenerator jg;
            if (datei.endsWith(MSearchConst.FORMAT_XZ)) {
                LZMA2Options options = new LZMA2Options();
                XZOutputStream out = new XZOutputStream(new FileOutputStream(file), options);
                jg = jsonF.createGenerator(out);
            } else if (datei.endsWith(MSearchConst.FORMAT_BZ2)) {
                bZip2CompressorOutputStream = new BZip2CompressorOutputStream(new FileOutputStream(file), 9 /*Blocksize: 1 - 9*/);
                jg = jsonF.createGenerator(bZip2CompressorOutputStream, JsonEncoding.UTF8);
            } else if (datei.endsWith(MSearchConst.FORMAT_ZIP)) {
                zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
                ZipEntry entry = new ZipEntry(MSearchConst.XML_DATEI_FILME);
                zipOutputStream.putNextEntry(entry);
                jg = jsonF.createGenerator(zipOutputStream, JsonEncoding.UTF8);
            } else {
                jg = jsonF.createGenerator(new File(datei), JsonEncoding.UTF8);
            }
            jg.useDefaultPrettyPrinter(); // enable indentation just to make debug/testing easier
            jg.writeStartObject();
            // Infos zur Filmliste
            jg.writeArrayFieldStart(ListeFilme.FILMLISTE);
            for (int i = 0; i < ListeFilme.MAX_ELEM; ++i) {
                jg.writeString(listeFilme.metaDaten[i]);
            }
            jg.writeEndArray();
            // Infos der Felder in der Filmliste
            jg.writeArrayFieldStart(ListeFilme.FILMLISTE);
            for (int i = 0; i < DatenFilm.MAX_ELEM; ++i) {
                jg.writeString(DatenFilm.COLUMN_NAMES[i]);
            }
            jg.writeEndArray();
            //Filme schreiben
            ListIterator<DatenFilm> iterator;
            DatenFilm datenFilm;
            iterator = listeFilme.listIterator();
            while (iterator.hasNext()) {
                datenFilm = iterator.next();
                jg.writeArrayFieldStart(DatenFilm.FILME_);
                for (int i = 0; i < DatenFilm.MAX_ELEM; ++i) {
                    if (i == DatenFilm.FILM_NR_NR) {
                        jg.writeString("");
                    } else if (i == DatenFilm.FILM_ABO_NAME_NR) {
                        jg.writeString("");
                    } else if (i == DatenFilm.FILM_SENDER_NR) {
                        if (datenFilm.arr[i].equals(sender)) {
                            jg.writeString("");
                        } else {
                            sender = datenFilm.arr[i];
                            jg.writeString(datenFilm.arr[i]);
                        }
                    } else if (i == DatenFilm.FILM_THEMA_NR) {
                        if (datenFilm.arr[i].equals(thema)) {
                            jg.writeString("");
                        } else {
                            thema = datenFilm.arr[i];
                            jg.writeString(datenFilm.arr[i]);
                        }
                    } else {
                        jg.writeString(datenFilm.arr[i]);
                    }
                }
                jg.writeEndArray();
            }
            jg.writeEndObject();
            jg.close();
            MSearchLog.systemMeldung("geschrieben!");
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(846930145, MSearchLog.FEHLER_ART_PROG, "IoXmlSchreiben.FilmeSchreiben", ex, "nach: " + datei);
        }
    }

    public void filmlisteSchreibenXml(String datei, ListeFilme listeFilme) {
        try {
            MSearchLog.systemMeldung("Filme Schreiben");
            xmlSchreibenStart(datei);
            xmlSchreibenFilmliste(listeFilme);
            xmlSchreibenEnde(datei);
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(846930145, MSearchLog.FEHLER_ART_PROG, "IoXmlSchreiben.FilmeSchreiben", ex, "nach: " + datei);
        }
    }

    private void xmlSchreibenStart(String datei) throws IOException, XMLStreamException {
        File file = new File(datei);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                MSearchLog.fehlerMeldung(947623049, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteSchreiben.xmlSchreibenStart", "Kann den Pfad nicht anlegen: " + dir.toString());
            }
        }
        MSearchLog.systemMeldung("Start Schreiben nach: " + datei);
        outFactory = XMLOutputFactory.newInstance();
        if (datei.endsWith(MSearchConst.FORMAT_BZ2)) {
            bZip2CompressorOutputStream = new BZip2CompressorOutputStream(new FileOutputStream(file), 9 /*Blocksize: 1 - 9*/);
            out = new OutputStreamWriter(bZip2CompressorOutputStream, MSearchConst.KODIERUNG_UTF);
        } else if (datei.endsWith(MSearchConst.FORMAT_ZIP)) {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry entry = new ZipEntry(MSearchConst.XML_DATEI_FILME);
            zipOutputStream.putNextEntry(entry);
            out = new OutputStreamWriter(zipOutputStream, MSearchConst.KODIERUNG_UTF);
        } else {
            out = new OutputStreamWriter(new FileOutputStream(file), MSearchConst.KODIERUNG_UTF);
        }
        writer = outFactory.createXMLStreamWriter(out);
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeCharacters("\n");//neue Zeile
        writer.writeStartElement(MSearchConst.XML_START);
        writer.writeCharacters("\n");//neue Zeile
    }

    private void xmlSchreibenFilmliste(ListeFilme listeFilme) throws XMLStreamException {
        //Filmliste Metadaten schreiben
        listeFilme.metaDaten[ListeFilme.FILMLISTE_VERSION_NR] = MSearchConst.VERSION_FILMLISTE;
        xmlSchreibenDaten(ListeFilme.FILMLISTE, ListeFilme.COLUMN_NAMES, listeFilme.metaDaten);
        // Feldinfo schreiben
        int xmlMax = DatenFilm.COLUMN_NAMES.length;
        try {
            writer.writeStartElement(DatenFilm.FELD_INFO);
            writer.writeCharacters("\n");//neue Zeile
            for (int i = 0; i < xmlMax; ++i) {
                writer.writeStartElement(DatenFilm.COLUMN_NAMES_[i]);
                writer.writeCharacters(DatenFilm.COLUMN_NAMES[i]);
                writer.writeEndElement();
                writer.writeCharacters("\n");//neue Zeile
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");//neue Zeile
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(638214005, MSearchLog.FEHLER_ART_PROG, "IoXmlSchreiben.xmlSchreibenFeldInfo", ex);
        }
        // Filme schreiben
        ListIterator<DatenFilm> iterator;
        DatenFilm datenFilm;
        String sender = "", thema = "";
        DatenFilm datenFilmSchreiben = new DatenFilm();
        iterator = listeFilme.listIterator();
        while (iterator.hasNext()) {
            datenFilm = iterator.next();
            for (int i = 0; i < datenFilm.arr.length; ++i) {
                datenFilmSchreiben.arr[i] = datenFilm.arr[i];
            }
            if (sender.equals(datenFilm.arr[DatenFilm.FILM_SENDER_NR])) {
                datenFilmSchreiben.arr[DatenFilm.FILM_SENDER_NR] = "";
            } else {
                sender = datenFilm.arr[DatenFilm.FILM_SENDER_NR];
            }
            if (thema.equals(datenFilm.arr[DatenFilm.FILM_THEMA_NR])) {
                datenFilmSchreiben.arr[DatenFilm.FILM_THEMA_NR] = "";
            } else {
                thema = datenFilm.arr[DatenFilm.FILM_THEMA_NR];
            }
            datenFilmSchreiben.clean();
            xmlSchreibenDaten(DatenFilm.FILME_, DatenFilm.COLUMN_NAMES_, datenFilmSchreiben.arr);
        }
    }

    private void xmlSchreibenDaten(String xmlName, String[] xmlSpalten, String[] datenArray) throws XMLStreamException {
        int xmlMax = datenArray.length;
        writer.writeStartElement(xmlName);
        for (int i = 0; i < xmlMax; ++i) {
            if (!datenArray[i].equals("")) {
                writer.writeStartElement(xmlSpalten[i]);
                writer.writeCharacters(datenArray[i]);
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
        writer.writeCharacters("\n");//neue Zeile
    }

    private void xmlSchreibenEnde(String datei) throws Exception {
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();
        if (datei.endsWith(MSearchConst.FORMAT_BZ2)) {
            writer.close();
            out.close();
            bZip2CompressorOutputStream.close();
        } else if (datei.endsWith(MSearchConst.FORMAT_ZIP)) {
            zipOutputStream.closeEntry();
            writer.close();
            out.close();
            zipOutputStream.close();
        } else {
            writer.close();
            out.close();
        }
        MSearchLog.systemMeldung("geschrieben!");
    }
}