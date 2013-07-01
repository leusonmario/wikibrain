package org.wikapidia.download;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.wikapidia.core.lang.Language;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ari Weiland
 *
 * A wrapper class for storing and processing information about a dump link.
 * Contains static parser methods to generate DumpLinkInfo instances, and
 * useful getters for all parameters plus custom information related to
 * processing and downloading a dump.
 *
 */
public class DumpLinkInfo {

    private static final Logger LOG = Logger.getLogger(DumpLinkGetter.class.getName());

    private final Language language;
    private final String date;
    private final LinkMatcher linkMatcher;
    private final URL url;
    private final int counter;
    private final String md5;

    public DumpLinkInfo(String langCode, String date, String linkMatcher, String url, String md5, int counter) throws MalformedURLException {
        this.language = Language.getByLangCode(langCode);
        this.date = date;
        this.linkMatcher = LinkMatcher.getByName(linkMatcher);
        this.url = new URL(url);
        this.md5 = md5;
        this.counter = counter;
    }

    public Language getLanguage() {
        return language;
    }

    public String getDate() {
        return date;
    }

    public LinkMatcher getLinkMatcher() {
        return linkMatcher;
    }

    public URL getUrl() {
        return url;
    }

    public int getCounter() {
        return counter;
    }

    public String getMd5() {
        return md5;
    }

    /**
     * Returns a string for the local path to save this dump file
     * @return
     */
    public String getLocalPath() {
        return language.getLangCode() + "/" + date;
    }

    /**
     * Returns a string for the file name to save this dump file
     * @return
     */
    public String getFileName() {
        return language.getLangCode() + "wiki." +
                linkMatcher.getName() + "." +
                counter + "." +
                date +
                getExtension();
    }

    /**
     * Returns a string for the extension to save this dump file
     * @return
     */
    public String getExtension() {
        String terminal = url.toString().substring(url.toString().lastIndexOf("wiki"));
        int first = terminal.indexOf(".");
        int last = terminal.lastIndexOf(".");
        if (first == last) {
            return terminal.substring(last); // Only 1 extension
        } else {
            return terminal.substring(first, first+4) + terminal.substring(last); // 2 extensions
        }
    }

    public String getDownloadName() {
        return url.toString().substring(url.toString().lastIndexOf("/") + 1);
    }

    /**
     * Parses a file of info pertaining to dump links into a list of DumpLinkInfo.
     * Info must be listed in order: lang code, date, LinkMatcher, URL
     * with each DumpLink reference on a new line.
     * @param file
     * @return
     */
    public static DumpLinkCluster parseFile(File file) {
        InputStream stream = null;
        Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();
        try {
            stream = FileUtils.openInputStream(file);
            List<String> lines = IOUtils.readLines(stream, "UTF-8");
            DumpLinkCluster dumpLinks = new DumpLinkCluster();
            for (String line : lines) {
                String[] parsedInfo = line.split("\t");
                String langCode     = parsedInfo[0];
                String date         = parsedInfo[1];
                String linkMatcher  = parsedInfo[2];
                String url          = parsedInfo[3];
                String md5          = parsedInfo[4];
                try {
                    if (!counters.containsKey(linkMatcher)) {
                        counters.put(linkMatcher, new AtomicInteger(0));
                    }
                    DumpLinkInfo temp = new DumpLinkInfo(
                            langCode,
                            date,
                            linkMatcher,
                            url,
                            md5,
                            counters.get(linkMatcher).getAndIncrement()
                    );
                    dumpLinks.add(temp);
                } catch (MalformedURLException e) {
                    LOG.log(Level.WARNING, "Malformed URL \"" + url + "\" : ", e);
                }
            }
            return dumpLinks;
        } catch (IOException e) {
            throw new RuntimeException(e);  // What else can we do?
        } finally {
            if (stream != null) IOUtils.closeQuietly(stream);
        }
    }
}
