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
import site.gutschi.solrexample.connectors.SolrConnector;
import site.gutschi.solrexample.model.Game;
import site.gutschi.solrexample.model.GameRepository;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameController {
    private final GameRepository gameRepository;
    private final SolrConnector solrConnector;

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
    public String showGames(@Param("team") String team, @Param("genre") String genre, @Param("decade") Integer decade, @Param("query") String query, Model model) {
        final var searchInput = SolrConnector.SearchInput.builder()
                .query(Optional.ofNullable(query).orElse("*"))
                .team(team)
                .genre(genre)
                .decade(decade)
                .build();
        final var searchResult = solrConnector.search(searchInput);
        model.addAttribute("query", query);
        model.addAttribute("team", team);
        model.addAttribute("decade", decade);
        model.addAttribute("genre", genre);
        model.addAttribute("games", getGames(searchResult.ids()));
        model.addAttribute("teams", searchResult.teams());
        model.addAttribute("genres", searchResult.genres());
        model.addAttribute("decades", searchResult.decades());
        return "games";
    }

    private Collection<Game> getGames(List<Integer> gameIds) {
        final var result = gameRepository.findAllById(gameIds);
        //findllById does not keep ordering, so reorder results
        return result.stream()
                .sorted(Comparator.comparingInt(g -> gameIds.indexOf(g.getId())))
                .collect(Collectors.toList());
    }
}
