package site.gutschi.solrexample.transport;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.gutschi.solrexample.connectors.CsvInputConnector;
import site.gutschi.solrexample.connectors.SolrConnector;
import site.gutschi.solrexample.model.Game;
import site.gutschi.solrexample.model.GameRepository;

import java.util.Collection;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/initialize")
public class InitializerController {
    private final GameRepository gameRepository;
    private final CsvInputConnector csvInputConnector;
    private final SolrConnector solrConnector;

    @GetMapping("")
    @Transactional
    public void init() {
        final var games = csvInputConnector.readCsv();
        initDB(games);
        initIndex(games);
        log.info("Initialized " + games.size() + " games");
    }

    private void initDB(Collection<Game> games) {
        gameRepository.truncate();
        gameRepository.saveAll(games);
    }

    public void initIndex(Collection<Game> games) {
        solrConnector.reindex(games);
    }
}
