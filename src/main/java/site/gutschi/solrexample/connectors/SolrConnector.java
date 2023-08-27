package site.gutschi.solrexample.connectors;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;
import site.gutschi.solrexample.model.Game;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class SolrConnector {
    @Builder
    public record SearchInput(@NonNull String query, Integer decade, String team, String genre){}
    @Builder
    public record SearchResult(@NonNull List<Integer> ids, Map<Integer, Integer> decades, Map<String, Integer> teams, Map<String, Integer> genres){}

    public SearchResult search(SearchInput searchInput) {
        try (SolrClient solr = new Http2SolrClient.Builder("http://localhost:8983/solr/games").build()) {
            final var solrQuery = generateQuery(searchInput);
            final var response = solr.query(solrQuery);
            return SearchResult.builder()
                    .ids(getDocumentIds(response))
                    .teams(getFacet(response, "team", String.class::cast))
                    .decades(getFacet(response, "decade", this::getYear))
                    .genres(getFacet(response, "genre", String.class::cast))
                    .build();
        } catch (IOException | SolrServerException e) {
            log.error("Could not search: " + searchInput.query, e);
            return SearchResult.builder().ids(List.of()).build();
        }
    }

    private SolrQuery generateQuery(SearchInput searchInput) {
        final var solrQuery = new SolrQuery();
        solrQuery.set("q", searchInput.query);
        solrQuery.set("fl", "id");
        solrQuery.set("df", "_text_");
        solrQuery.set("sort", "score desc");
        solrQuery.setRows(2000);
        Optional.ofNullable(searchInput.team()).ifPresent(t ->
                solrQuery.addFilterQuery("team:" + t)
        );
        Optional.ofNullable(searchInput.genre()).ifPresent(g ->
                solrQuery.addFilterQuery("genre:" + g)
        );
        Optional.ofNullable(searchInput.decade()).ifPresent( y -> {
                    final var start = y +"-01-01T00:00:00Z";
                    final var end = (y+10) +"-01-01T00:00:00Z";
                    final var range = "["+ start + " TO " + end + "]";
                    solrQuery.addFilterQuery("releaseDate:" + range);
                }
        );
        solrQuery.setFacet(true);
        solrQuery.set("json.facet", """
                    {
                    "team": {
                         "type": "terms",
                          "field" : "team",
                    },
                    "genre": {
                         "type": "terms",
                          "field" : "genre",
                    },
                    "decade": {
                          "type": "range",
                          "field": "releaseDate"
                          "start":"1980-01-01T12:00:00Z"
                          "end":"2030-01-01T12:00:00Z"
                          "mincount" : 1,
                          "gap":"+10YEAR",
                     }}""");
        return solrQuery;
    }

    private List<Integer> getDocumentIds(QueryResponse queryResponse) {
        final var ids = queryResponse.getResults().stream()
                .map(s -> (String) s.getFieldValue("id"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        log.info("Get " + ids.size() + " games");
        return ids;
    }

    private <T> Map<T, Integer> getFacet(QueryResponse queryResponse, String name, Function<Object,T> value) {
        final var facets = queryResponse.getJsonFacetingResponse().getBucketBasedFacets(name);
        if(facets == null) {
            return Map.of();
        }
        final var result = new LinkedHashMap<T, Integer>();
        facets.getBuckets().forEach(b -> {
            final var val = value.apply(b.getVal());
            final var count = (int) b.getCount();
            result.put(val, count);
        });
        return result;
    }

    private int getYear(Object dateObj){
        final var date = (Date) dateObj;
        final var c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.YEAR);
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
        Optional.ofNullable(game.getReleaseDate())
                //Solr always needs date AND time
                .map(d -> d + "T12:00:00Z")
                .ifPresent(d -> document.addField("releaseDate", d));
        return document;
    }
}
