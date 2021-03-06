package org.wikibrain.core.model;

import org.wikibrain.core.lang.Language;
import org.wikibrain.core.lang.LocalId;

/**
 */
public class LocalPage {

    protected final Language language;
    protected final int localId;
    protected final Title title;
    protected final NameSpace nameSpace;
    protected final boolean isRedirect;
    protected final boolean isDisambig;

    /**
     * Creates a new page in the main namespace that is NOT a redirect or disambig.
     * @param language
     * @param localId
     * @param title
     */
    public LocalPage(Language language, int localId, String title) {
        this(language, localId, new Title(title, language), NameSpace.ARTICLE);
    }

    /**
     * Default for NON-redirect pages.
     * @param language
     * @param localId
     * @param title
     * @param nameSpace
     */
    public LocalPage(Language language, int localId, Title title, NameSpace nameSpace){
        this.language = language;
        this.localId = localId;
        this.title = title;
        this.nameSpace = nameSpace;
        isRedirect = false;
        isDisambig = false;
    }

    /**
     * Ability to set redirect pages.
     * @param language
     * @param localId
     * @param title
     * @param nameSpace
     * @param redirect
     */
    public LocalPage(Language language, int localId, Title title, NameSpace nameSpace, boolean redirect, boolean disambig) {
        this.language = language;
        this.localId = localId;
        this.title = title;
        this.nameSpace = nameSpace;
        isRedirect = redirect;
        isDisambig = disambig;
    }

    public int getLocalId() {
        return localId;
    }

    public Title getTitle() {
        return title;
    }

    public Language getLanguage() {
        return language;
    }

    public NameSpace getNameSpace() {
        return nameSpace;
    }

    public boolean isDisambig() {
        return isDisambig;
    }

    public boolean isRedirect() {
        return isRedirect;
    }

    public int hashCode(){
        return (language.getId() + "_" + localId).hashCode(); //non-optimal
    }

    public LocalId toLocalId() {
        return new LocalId(language, localId);
    }

    public boolean equals(Object o){
        if (o instanceof LocalPage){
            LocalPage input = (LocalPage)o;
            return (input.getLanguage().equals(this.getLanguage()) &&
                    input.getLocalId() == this.getLocalId()
            );
        } else {
            return false;
        }
    }

    /**
     * @return, for example "/w/en/1000/Hercule_Poirot"
     */
    public String getCompactUrl() {
        String escapedTitle = getTitle().getCanonicalTitle().replace(" ", "_");
        escapedTitle = escapedTitle.replaceAll("\\s+", "");
        return "/w/" + getLanguage().getLangCode() + "/" + getLocalId() + "/" + escapedTitle;
    }

    @Override
    public String toString() {
        return "LocalPage{" +
                "nameSpace=" + nameSpace +
                ", title=" + title +
                ", localId=" + localId +
                ", language=" + language +
                '}';
    }
}
