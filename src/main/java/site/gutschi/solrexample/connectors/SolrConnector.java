package site.gutschi.solrexample.connectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;
import site.gutschi.solrexample.model.Game;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class SolrConnector {
    public Collection<Integer> search(String search) {
        try (SolrClient solr = new Http2SolrClient.Builder("http://localhost:8983/solr/games").build()) {
            final var solrQuery = new SolrQuery();
            solrQuery.set("q", search);
            solrQuery.set("fl", "id");
            solrQuery.set("df", "title");
            solrQuery.setRows(1000);
            final var response = solr.query(solrQuery);
            final var ids = response.getResults().stream()
                    .map(s -> (String) s.getFieldValue("id"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            log.info("Get " + ids.size() + " games");
            return ids;
        } catch (IOException | SolrServerException e) {
            log.error("Could not search: " + search, e);
            return List.of();
        }
    }

    public void reindex(Collection<Game> games) {
        try (SolrClient solr = new Http2SolrClient.Builder("http://localhost:8983/solr/games").build()) {
            solr.deleteByQuery("*.*");
            games.forEach(game -> {
                try {
                    SolrInputDocument document = convertToSolr(game);
                    solr.add(document);
                } catch (SolrServerException | IOException e) {
                    log.warn("Could not index " + game, e);
                }
            });
            log.info("Added {} games to solr index", games.size());
            solr.commit();
        } catch (IOException | SolrServerException e) {
            log.warn("Could not re-index ", e);
        }
    }

    private SolrInputDocument convertToSolr(Game game) {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", game.getId());
        document.addField("title", game.getTitle());
        game.getTeam().forEach(t -> document.addField("team", t));
        game.getGenres().forEach(g -> document.addField("genre", g));
        document.addField("summary", game.getSummary());
        return document;
    }
}
