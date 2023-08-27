package site.gutschi.solrexample.connectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.gutschi.solrexample.model.Game;
import site.gutschi.solrexample.model.GameRepository;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class SolrConnector {
    private final GameRepository gameRepository;

    public List<Integer> search(String search) {
        //Not Implemented yet, just return all games
        return gameRepository.findAll().stream().map(Game::getId).toList();
    }

    public void reindex(Collection<Game> games) {
        //Not Implemented yet
    }
}
