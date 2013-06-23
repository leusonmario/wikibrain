package org.wikapidia.core.dao.sql;

import com.typesafe.config.Config;
import org.apache.commons.io.IOUtils;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.wikapidia.conf.Configuration;
import org.wikapidia.conf.ConfigurationException;
import org.wikapidia.conf.Configurator;
import org.wikapidia.core.dao.*;
import org.wikapidia.core.jooq.Tables;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.model.LocalPage;
import org.wikapidia.core.model.NameSpace;
import org.wikapidia.core.model.RawPage;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Retrieves and stores page text.
 * Wraps a LocalPageDao to build a full RawPage.
 */
public class RawPageSqlDao extends AbstractSqlDao implements RawPageDao {

    public RawPageSqlDao(DataSource dataSource) throws DaoException {
        super(dataSource);
    }

    @Override
    public void beginLoad() throws DaoException {
        Connection conn=null;
        try {
            conn = ds.getConnection();
            conn.createStatement().execute(
                    IOUtils.toString(
                            RawPageSqlDao.class.getResource("/db/raw-page-schema.sql")
                    ));
        } catch (IOException e) {
            throw new DaoException(e);
        } catch (SQLException e){
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    @Override
    public void save(RawPage page) throws DaoException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DSLContext context = DSL.using(conn, dialect);
            context.insertInto(Tables.RAW_PAGE).values(
                    page.getLang().getId(),
                    page.getPageId(),
                    page.getRevisionId(),
                    page.getBody(),
                    page.getTitle(),
                    page.getLastEdit(),
                    page.getNamespace().getArbitraryId(),
                    page.isRedirect(),
                    page.isDisambig(),
                    page.getRedirectTitle()
            ).execute();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    @Override
    public void endLoad() throws DaoException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            conn.createStatement().execute(
                    IOUtils.toString(
                            RawPageSqlDao.class.getResource("/db/raw-page-indexes.sql")
                    ));
        } catch (IOException e) {
            throw new DaoException(e);
        } catch (SQLException e){
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    private RawPage buildRawPage(Record record){
        Timestamp timestamp = record.getValue(Tables.RAW_PAGE.LASTEDIT);
        return new RawPage(record.getValue(Tables.RAW_PAGE.PAGE_ID),
                record.getValue(Tables.RAW_PAGE.REVISION_ID),
                record.getValue(Tables.RAW_PAGE.TITLE),
                record.getValue(Tables.RAW_PAGE.BODY),
                new Date(timestamp.getTime()),
                Language.getById(record.getValue(Tables.RAW_PAGE.LANG_ID)),
                NameSpace.getNameSpaceById(record.getValue(Tables.RAW_PAGE.NAME_SPACE)),
                record.getValue(Tables.RAW_PAGE.IS_REDIRECT),
                record.getValue(Tables.RAW_PAGE.IS_DISAMBIG),
                record.getValue(Tables.RAW_PAGE.REDIRECT_TITLE)
        );
    }

    @Override
    public RawPage get(Language language, int rawLocalPageId) throws DaoException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DSLContext context = DSL.using(conn, dialect);
            return buildRawPage(context.
                    select().
                    from(Tables.RAW_PAGE).
                    where(Tables.RAW_PAGE.PAGE_ID.eq(rawLocalPageId)).
                    and(Tables.RAW_PAGE.LANG_ID.eq(language.getId())).
                    fetchOne());
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    @Override
    public String getBody(Language language, int rawLocalPageId) throws DaoException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DSLContext context = DSL.using(conn, dialect);
            return context.
                select().
                from(Tables.RAW_PAGE).
                where(Tables.RAW_PAGE.PAGE_ID.eq(rawLocalPageId)).
                and(Tables.RAW_PAGE.LANG_ID.eq(language.getId())).
                fetchOne().
                getValue(Tables.RAW_PAGE.BODY);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    @Override
    public SqlDaoIterable<RawPage> allRawPages() throws DaoException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DSLContext context =  DSL.using(conn, dialect);
            TableField idField;
            Cursor<Record> result = context.select()
                    .from(Tables.RAW_PAGE)
                    .fetchLazy(getFetchSize());
            return new SqlDaoIterable<RawPage>(result) {
                @Override
                public RawPage transform(Record r) {
                     return buildRawPage(r);
                }
            };
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public SqlDaoIterable<RawPage> getAllRedirects(Language language) throws DaoException{
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DSLContext context = DSL.using(conn, dialect);
            Cursor<Record> result = context.select().
                    from(Tables.RAW_PAGE).
                    where(Tables.RAW_PAGE.LANG_ID.eq(language.getId())).
                    and(Tables.RAW_PAGE.IS_REDIRECT.equal(true)).
                    fetchLazy();
            return new SqlDaoIterable<RawPage>(result) {
                @Override
                public RawPage transform(Record r) {
                    return buildRawPage(r);
                }
            };
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    public static class Provider extends org.wikapidia.conf.Provider<RawPageDao> {
        public Provider(Configurator configurator, Configuration config) throws ConfigurationException {
            super(configurator, config);
        }

        @Override
        public Class<RawPageDao> getType() {
            return RawPageDao.class;
        }

        @Override
        public String getPath() {
            return "dao.rawPage";
        }

        @Override
        public RawPageDao get(String name, Config config) throws ConfigurationException {
            if (!config.getString("type").equals("sql")) {
                return null;
            }
            try {
                return new RawPageSqlDao(
                        getConfigurator().get(
                                DataSource.class,
                                config.getString("dataSource"))
                        );
            } catch (DaoException e) {
                throw new ConfigurationException(e);
            }
        }
    }
}