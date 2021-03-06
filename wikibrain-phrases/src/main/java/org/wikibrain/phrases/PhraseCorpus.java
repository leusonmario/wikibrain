package org.wikibrain.phrases;

import org.wikibrain.core.dao.DaoException;
import org.wikibrain.phrases.PhraseAnalyzerDao;

import java.io.IOException;

/**
 */
public interface PhraseCorpus {
    /**
     * Loads a single phrase corpus into the database.
     * @throws java.io.IOException, DaoException
     */
    void loadCorpus(PhraseAnalyzerDao dao) throws DaoException, IOException;
}
