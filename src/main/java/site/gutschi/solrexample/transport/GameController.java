package site.gutschi.solrexample.transport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;
import site.gutschi.solrexample.model.Game;
import site.gutschi.solrexample.model.GameRepository;

import java.util.Collection;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameController {
    private final GameRepository gameRepository;

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/games/{id}")
    public String showGame(@PathVariable("id") int id, Model model) {
        log.info("Get game " + id);
        final var game = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("game", game);
        return "game";
    }

    @GetMapping("/")
    public RedirectView redirectRoot() {
        return new RedirectView("/games");
    }

    @GetMapping("/games/")
    public RedirectView redirectSlash() {
        return new RedirectView("/games");
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/games")
    public String showGames(@Param("search") String search, Model model) {
        final var games = getGames(search);
        model.addAttribute("games", games);
        model.addAttribute("search", search );
        return "games";
    }

    private Collection<Game> getGames(String search) {
        if (search == null) {
            log.info("Get all games");
            return gameRepository.findAll();
        }
        return List.of();

        /*
        try (SolrClient solr = new Http2SolrClient.Builder("http://localhost:8983/solr/games").build()){
            final var solrQuery = new SolrQuery();
            solrQuery.set("q", search);
            solrQuery.set("fl", "id");
            solrQuery.setRows(1000);
            final var response = solr.query(solrQuery);
            final var ids = response.getResults().stream()
                    .map(s -> (String) s.getFieldValue("id"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            log.info("Get " + ids.size() + " games");
            return gameRepository.findAllById(ids);
        } catch (IOException | SolrServerException e) {
            log.error("Could not search: " + search, e);
            return List.of();
        }
         */
    }
}
