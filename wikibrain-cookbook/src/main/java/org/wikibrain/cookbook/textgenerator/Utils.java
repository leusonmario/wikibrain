package org.wikibrain.cookbook.textgenerator;

import org.wikibrain.core.lang.Language;

import java.util.Arrays;
import java.util.List;

/**
 * Utilities for HW7
 * @author Shilad Sen
 */
public class Utils {

    /**
     * YOU MUST Update this to point to your path
     */
    public static final String PATH_DB = "../";

    /**
     * Individual languages installed in the database.
     */
    public static final Language LANG_WELSH = Language.getByLangCode("cy");
    public static final Language LANG_SIMPLE = Language.getByLangCode("simple");
    public static final Language LANG_HINDI = Language.getByLangCode("hi");
    public static final Language LANG_BOSNIAN = Language.getByLangCode("bs");
    public static final Language LANG_ICELANDIC = Language.getByLangCode("is");
    public static final Language LANG_SCOTS = Language.getByLangCode("sco");

    /**
     * A list of all installed languages.
     */
    public static final List<Language> ALL_LANGS = Arrays.asList(
            LANG_WELSH, LANG_SIMPLE, LANG_HINDI,
            LANG_BOSNIAN, LANG_ICELANDIC, LANG_SCOTS);
}
